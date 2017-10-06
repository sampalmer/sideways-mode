package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * UI proxy to settings stored in background process.
 * Also supports showing an error message at the bottom of the activity.
 */
//TODO: Improve the name of this, or reorganise it.
public abstract class PreferencesActivity extends UiActivity {
	
	private ApplicationStatusService.Client serviceClient;
	private Snackbar snackbar;

	@Override
	protected void onStart() {
		super.onStart();
		
		boolean boundSuccessfully = bindService(
			new Intent(this, ApplicationStatusService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
		
		if (!boundSuccessfully) {
			// The documentation says you should still unbind after a binding failure
			this.unbindService(serviceConnection);
			throw new RuntimeException("Couldn't connect to " + ApplicationStatusService.class.getSimpleName());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (serviceClient != null) {
			try {
				serviceClient.unsubscribe();
			} catch (RemoteException ignored) {
				// The service process died, but that doesn't matter since we're unsubscribing.
			}
			
			serviceClient = null;
		}
		unbindService(serviceConnection);
	}
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceClient = new ApplicationStatusService.Client(iBinder, statusChanged);
			
			try {
				serviceClient.subscribe();
			} catch (RemoteException e) {
				// Service process died, so we'll have to try again once it's back up.
				serviceClient = null;
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceClient = null;
		}
		
	};
	
	private final ApplicationStatusService.Client.Listener statusChanged = new ApplicationStatusService.Client.Listener() {
		@Override
		public void onChange(ApplicationStatus status) {
			final EnvironmentError lastError = status.error;

			if (snackbar == null) {
				//http://stackoverflow.com/a/5069354/238753
				View contentView = findViewById(R.id.coordinator_layout);
				if (contentView == null)
					throw new RuntimeException("Didn't find any elements with the coordinator layout id");

				if (!(contentView instanceof CoordinatorLayout))
					throw new RuntimeException("Content layout should be a " + CoordinatorLayout.class.getSimpleName() + " so the environment error snackbar can be dismissed. Instead found a " + contentView.getClass().getName() + ".");

				snackbar = Snackbar.make(contentView, "", Snackbar.LENGTH_INDEFINITE);
				snackbar.addCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						super.onDismissed(snackbar, event);

						if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_ACTION)
							new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(PreferencesActivity.this);

						//If the user swipes away the snackbar, it can't be shown anymore, so this one is effectively dead
						PreferencesActivity.this.snackbar = null;
					}
				});
			}

			if (lastError == null)
				snackbar.dismiss();
			else {
				snackbar.setText(lastError.getSummary());
				snackbar.setAction(R.string.environment_error_more, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							lastError.showError().send();
						} catch (PendingIntent.CanceledException e) {
							throw new RuntimeException(e);
						}
					}
				});
				snackbar.show();
			}
		}
	};

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		if (name.equals(ApplicationSettings.sharedPreferencesName()))
			return ApplicationSettings.instance().sharedPreferences;
		else
			return super.getSharedPreferences(name, mode);
	}

}
