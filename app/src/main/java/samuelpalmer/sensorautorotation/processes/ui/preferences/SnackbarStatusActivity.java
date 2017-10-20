package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.app.PendingIntent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.ApplicationStatus;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

/**
 * Supports showing an error message at the bottom of the activity.
 */
public abstract class SnackbarStatusActivity extends UiActivity {
	
	private ApplicationStatusClient statusClient;
	private Snackbar snackbar;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		statusClient = new ApplicationStatusClient(this, statusChanged);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		statusClient.subscribe();
	}

	@Override
	protected void onStop() {
		super.onStop();
		statusClient.unsubscribe();
	}
	
	private final ApplicationStatusClient.Listener statusChanged = new ApplicationStatusClient.Listener() {
		@Override
		public void onChange(ApplicationStatus status) {
			if (status == null)
				return;
			
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
							new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(SnackbarStatusActivity.this);

						//If the user swipes away the snackbar, it can't be shown anymore, so this one is effectively dead
						SnackbarStatusActivity.this.snackbar = null;
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
	
}
