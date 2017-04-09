package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.DummyActivity;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardDialogCoordinator;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardDialog;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {

	private ServiceStatusContentProvider.Client statusClient;
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

			//Caching and reusing the dialog to prevent multiple dialogs from being open at the same time.
			if (!DashboardDialogCoordinator.dialogExists()) {
				DashboardDialog dialog = new DashboardDialog.Builder(getApplicationContext()).create();
				Bundle args = new Bundle();
				Dialog dashboard = dialog.onCreateDialog(args);
				dashboard.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						DashboardDialogCoordinator.destroyed();
					}
				});

				//Once the dialog had gone but the field was still set. Maybe the below will help with that?
				dashboard.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						DashboardDialogCoordinator.destroyed();
					}
				});

				showDialog(dashboard);
			}
			else {
				DashboardDialogCoordinator.showDialog();
				startActivityAndCollapse(new Intent(this, DummyActivity.class)); //To collapse the navigation drawer
			}

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

		statusClient = new ServiceStatusContentProvider.Client(this);
		statusClient.subscribe(statusListener);

		enabledSetting = ApplicationSettings.remote().enabled();
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

	private final IServiceStatusListener.Stub statusListener = new IServiceStatusListener.Stub() {
		@Override
		public void onChange(ServiceStatus status) throws RemoteException {
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

	private void updateTile(ServiceStatus status) {
		Tile tile = getQsTile();
		if (tile == null)
			throw new RuntimeException("Couldn't get quick setting tile. Maybe the service isn't in listening state?");

		//STATE_UNAVAILABLE prevents the onClick() callback from firing, so using STATE_INACTIVE instead
		tile.setState(status == null ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);

		//noinspection ResourceType
		tile.setIcon(Icon.createWithResource(this, status == null ? R.drawable.notification_rotation : status.mode.notificationIcon()));

		tile.setLabel(status == null ? StringResource.get(R.string.app_name_full) : status.getSummary());
		tile.updateTile(); //Seems to subsequently cause loss of listening state.
	}

}
