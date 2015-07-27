package samuelpalmer.sensorautorotation.notifications.service;

import android.app.PendingIntent;
import android.content.Intent;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import cyanogenmod.app.CustomTile.Builder;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.Reflection;
import samuelpalmer.sensorautorotation.processes.service.ToggleRotationLockReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

public class QuickSetting {

	//Don't change this or the user could end up with multiple tiles on upgrade
	public static final int TILE_ID = 1;
	private final boolean supported = Reflection.classExists("cyanogenmod.app.CustomTile");

	public void update(NotificationParameters params) {
		if (supported) {
			CustomTile tile = tile(params);
			statusBarManager().publishTile(TILE_ID, tile);
		}
	}

	public void cancel() {
		if (supported)
			statusBarManager().removeTile(TILE_ID);
	}

	private CustomTile tile(NotificationParameters params) {
		return new Builder(Process.context())
				.setIcon(params.mode.iconResourceId(params.overridden))
				.setLabel(params.summary.toString())
				.setOnClickIntent(toggleIntent())
				.setOnSettingsClickIntent(settingsIntent())
				.build();
	}

	private PendingIntent toggleIntent() {
		return ComponentType.create(ToggleRotationLockReceiver.class).pendingIntent();
	}

	private Intent settingsIntent() {
		return ComponentType.create(MainActivity.class).intent();
	}

	private CMStatusBarManager statusBarManager() {
		return CMStatusBarManager.getInstance(Process.context());
	}

}
