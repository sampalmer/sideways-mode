package samuelpalmer.sensorautorotation.notifications.service;

import android.app.PendingIntent;
import android.content.Intent;

import java.util.Collections;
import java.util.Set;

import cyanogenmod.app.CMStatusBarManager;
import cyanogenmod.app.CustomTile;
import cyanogenmod.app.CustomTile.Builder;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.Reflection;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.ToggleRotationLockReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

public class QuickSettingServiceStatusDisplay extends ServiceStatusDisplay {

	//Don't change this or the user could end up with multiple tiles on upgrade
	public static final int TILE_ID = 1;

	public static boolean isSupported() {
		return Reflection.classExists("cyanogenmod.app.CustomTile");
	}

	@Override
	public void cancel() {
		statusBarManager().removeTile(TILE_ID);
	}

	@Override
	public void update(NotificationParameters params) {
		CustomTile tile = tile(params);
		statusBarManager().publishTile(TILE_ID, tile);
	}

	@Override
	public Set<EventRegistrar> updateTriggers() {
		return Collections.emptySet();
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
