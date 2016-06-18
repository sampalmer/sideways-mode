package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.NotificationParameters;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplay;

//TODO: The tile seems to remain unupdated until a moment after the notification drawer is pulled down. Maybe make this an "active" tile service?
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {

	private final ServiceStatusDisplay notificationParamsChanged = new ServiceStatusDisplay() {
		@Override
		public void update(NotificationParameters params) {
			updateTile();
		}
	};

	private final EventHandler serviceStateChanged = new EventHandler() {
		@Override
		public void update() {
			updateTile();
			listenForNotificationParamsChanges();
		}
	};

	private void listenForNotificationParamsChanges() {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().serviceNotificationManager.onParamsChanged.add(notificationParamsChanged);
	}

	/**
	 * Doesn't seem to fire if when the tile is in STATE_UNAVAILABLE
	 */
	@Override
	public void onClick() {
		super.onClick();

		RotationServiceClient.send(RotationService.ACTION_TOGGLE);

		//TODO: Change this to show a dialog/activity allowing access to the rotate actions
//		if (isLocked())
//			startActivity();
//		else
//			showDialog(null);

		//TODO: Maybe do something different if the service isn't actually running? Or maybe it already shows the toast and is OK?
	}

	@Override
	public void onTileAdded() {
		super.onTileAdded();

		//TODO: If tile is an active tile, request listening state so it can be updated.
	}

	@Override
	public void onStartListening() {
		super.onStartListening();

		updateTile();
		ServiceHolder.onServiceStateChange.subscribe(serviceStateChanged);
		listenForNotificationParamsChanges();
	}

	@Override
	public void onStopListening() {
		super.onStopListening();

		ServiceHolder.onServiceStateChange.unsubscribe(serviceStateChanged);
	}

	private void updateTile() {
		NotificationParameters parameters = null;

		if (ServiceHolder.isRunning()) {
			RotationService service = ServiceHolder.instance();
			parameters = service.notificationParametersFactory.generate();
		}

		//TODO: Remove duplication of default tile properties in manifest
		Tile tile = getQsTile();

		//STATE_UNAVAILABLE prevents the onClick() callback from firing, so using STATE_INACTIVE instead
		tile.setState(parameters == null ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
		//noinspection ResourceType
		tile.setIcon(Icon.createWithResource(this, parameters == null ? R.drawable.rotation : parameters.mode.actionIcon()));
		tile.setLabel(parameters == null ? StringResource.get(R.string.app_name) : parameters.getSummary());
		tile.updateTile();
	}

}
