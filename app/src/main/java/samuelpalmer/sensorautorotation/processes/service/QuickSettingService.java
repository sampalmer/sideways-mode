package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.controlpanel.ControlPanelCoordinator;
import samuelpalmer.sensorautorotation.processes.service.controlpanel.ControlPanelDialog;
import samuelpalmer.sensorautorotation.processes.service.controlpanel.ControlPanelActivity;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {

	/**
	 * Doesn't seem to fire when the tile is in STATE_UNAVAILABLE
	 */
	@Override
	public void onClick() {
		super.onClick();

		Log.i(QuickSettingService.class.getSimpleName(), "Clicked");

		if (isLocked()) {
			startActivityAndCollapse(new Intent(this, ControlPanelActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		else {

			//Caching and reusing the dialog to prevent multiple dialogs from being open at the same time.
			if (!ControlPanelCoordinator.exists()) {
				ControlPanelDialog dialog = new ControlPanelDialog.Builder(getApplicationContext()).create();
				Bundle args = new Bundle();
				Dialog popupControlPanel = dialog.onCreateDialog(args);
				popupControlPanel.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						ControlPanelCoordinator.destroyed();
					}
				});

				//Once the dialog had gone but the field was still set. Maybe the below will help with that?
				popupControlPanel.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						ControlPanelCoordinator.destroyed();
					}
				});

				/* This makes the service process's RAM usage jump up from ~8MB to ~14MB,
				 * but we can't use an activity because the OS delays activity start
				 * when the home button is pressed.
				 *
				 * https://code.google.com/p/android/issues/detail?id=4536
				 */
				showDialog(popupControlPanel);
			}
			else {
				ControlPanelCoordinator.show();
				startActivityAndCollapse(new Intent(this, DummyActivity.class)); //To collapse the navigation drawer
			}
		}
	}

	@Override
	public void onTileAdded() {
		super.onTileAdded();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile added");

		requestTileUpdate();
	}

	@Override
	public void onTileRemoved() {
		super.onTileRemoved();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile removed");
	}

	private static QuickSettingService listeningInstance;

	public static void requestTileUpdate() {
		if (listeningInstance == null)
			requestListeningState();
		else
			listeningInstance.updateTile();
	}

	/**
	 * Note that this seems to provide listening state for _one_ tile update. Then listening state
	 * is lost and must be re-requested.
	 *
	 * Seems to have no effect when the tile isn't actually added.
	 */
	private static void requestListeningState() {
		ComponentName componentName = new ComponentName(CurrentProcess.context(), QuickSettingService.class);
		requestListeningState(CurrentProcess.context(), componentName);
	}

	@Override
	public void onStartListening() {
		super.onStartListening();

		listeningInstance = this;

		updateTile();
	}

	@Override
	public void onStopListening() {
		super.onStopListening();

		listeningInstance = null;
	}

	private void updateTile() {
		//TODO: Only update when the service running state or serviceStatus have changed.

		if (listeningInstance == null)
			throw new RuntimeException("Not allowed to update the tile outside of listening state.");

		ServiceStatus parameters = makeServiceStatusIfAvailable();

		Tile tile = getQsTile();
		if (tile == null)
			throw new RuntimeException("Couldn't get quick setting tile. Maybe the service isn't in listening state?");

		//STATE_UNAVAILABLE prevents the onClick() callback from firing, so using STATE_INACTIVE instead
		tile.setState(parameters == null ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);

		//TODO: Maybe add a "plus" symbol to the icon to distinguish it from the Android one. Also do this for the app's main logo.
		//noinspection ResourceType
		tile.setIcon(Icon.createWithResource(this, parameters == null ? R.drawable.rotation : parameters.mode.notificationIcon()));

		tile.setLabel(parameters == null ? StringResource.get(R.string.app_name_full) : parameters.getSummary());
		tile.updateTile(); //Seems to subsequently cause loss of listening state.
	}

	private ServiceStatus makeServiceStatusIfAvailable() {
		if (ServiceHolder.isRunning()) {
			RotationService service = ServiceHolder.instance();
			return service.serviceStatusFactory.generate();
		}

		return null;
	}

}
