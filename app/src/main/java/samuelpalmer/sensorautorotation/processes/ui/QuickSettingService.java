package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.DummyActivity;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IApplicationStatusListener;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardDialogCoordinator;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {

	private ApplicationStatusContentProvider.Client statusClient;
	private Settings.Setting<Boolean> enabledSetting;

	/**
	 * Doesn't seem to fire when the tile is in STATE_UNAVAILABLE
	 */
	@Override
	public void onClick() {
		super.onClick();

		Log.i(QuickSettingService.class.getSimpleName(), "Clicked");

		if (isLocked()) {
			//Not using the dialog here since dialogs don't show on the lock screen.
			startActivityAndCollapse(new Intent(this, DashboardActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {
			//We can't use an activity because the OS delays activity start when the home button is pressed.
			
			//Caching and reusing the dialog to prevent multiple dialogs from being open at the same time.
			if (!DashboardDialogCoordinator.dialogExists()) {
				Dialog dashboard = DashboardDialogCoordinator.createDialog(getApplicationContext(), new Runnable() {
					@Override
					public void run() {}
				});
				
				tryShowDialog(dashboard);
			}
			else {
				DashboardDialogCoordinator.showDialog();
				startActivityAndCollapse(new Intent(this, DummyActivity.class)); //To collapse the navigation drawer
			}

		}
	}
	
	private void tryShowDialog(Dialog dashboard) {
		for (int i = 0; i < 3; ++i)
			try {
				showDialog(dashboard);
				return;
			}
			catch (RuntimeException ex) {
				// There's an Android bug that causes showDialog() to crash sometimes: https://issuetracker.google.com/issues/37105965
				// Retrying just in case it's an intermittent issue.
				if (ex.getClass().getName().equals("android.view.WindowManager$BadTokenException")) {
					if (i >= 2)
						throw ex;
					else
						Log.w(QuickSettingService.class.getSimpleName(), "Encountered OS bug when showing quick setting dialog. Retrying...", ex);
				}
				else
					throw ex;
			}
	}
	
	@Override
	public void onTileAdded() {
		super.onTileAdded();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile added");
	}

	@Override
	public void onTileRemoved() {
		super.onTileRemoved();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile removed");
	}

	@Override
	public void onStartListening() {
		super.onStartListening();

		statusClient = new ApplicationStatusContentProvider.Client(this);
		statusClient.subscribe(statusListener);
		
		enabledSetting = ApplicationSettings.instance().enabled();
		enabledSetting.subscribe(enabledSettingListener);

		enabledSettingListener.update();
	}

	@Override
	public void onStopListening() {
		super.onStopListening();

		enabledSetting.unsubscribe(enabledSettingListener);
		enabledSetting = null;

		statusClient.unsubscribe(statusListener);
		statusClient = null;
	}

	private final IApplicationStatusListener.Stub statusListener = new IApplicationStatusListener.Stub() {
		@Override
		public void onChange(ApplicationStatus status) throws RemoteException {
			if (statusClient != null) //Just in case we've lost listening state
				updateTile(status);
		}
	};

	private final EventHandler enabledSettingListener = new EventHandler() {
		@Override
		public void update() {
			if (!enabledSetting.get())
				updateTile(null);
		}
	};

	private void updateTile(ApplicationStatus status) {
		Tile tile = getQsTile();
		if (tile == null)
			throw new RuntimeException("Couldn't get quick setting tile. Maybe the service isn't in listening state?");

		//STATE_UNAVAILABLE prevents the onClick() callback from firing, so using STATE_INACTIVE instead
		tile.setState(status == null ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);

		//noinspection ResourceType
		tile.setIcon(Icon.createWithResource(this, R.drawable.notification_rotation));

		tile.setLabel(StringResource.get(status == null ? R.string.app_name : status.user.descriptionId()));
		tile.updateTile(); //Seems to subsequently cause loss of listening state.
	}

}
