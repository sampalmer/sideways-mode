package samuelpalmer.sensorautorotation.notifications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final Context context;
	private final ServiceNotification serviceNotification;
	private final ScreenOrientationUpdater screen;
	private final LockScreenHider notificationHider;
	private final EventRegistrar<Void> onShowNotificationAtTopChanged;

	public ServiceNotificationManager(Context context, ServiceNotification serviceNotification, ApplicationSettings settings, ScreenOrientationUpdater screen) {
		this.context = context;
		this.serviceNotification = serviceNotification;
		this.screen = screen;
		notificationHider = new LockScreenHider(context, serviceNotification);
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
	}

	@Override
	protected void start() {
		serviceNotification.set();
		notificationHider.ensureStarted();
		onShowNotificationAtTopChanged.subscribe(updateNotification);
		screen.onChange().subscribe(updateNotification);
		context.registerReceiver(screenRotationMightHaveChanged, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(screenRotationMightHaveChanged);
		screen.onChange().unsubscribe(updateNotification);
		onShowNotificationAtTopChanged.unsubscribe(updateNotification);
		notificationHider.ensureStopped();
	}

	private final EventHandler<Void> updateNotification = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			serviceNotification.set();
		}
	};

	private final BroadcastReceiver screenRotationMightHaveChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			serviceNotification.set();
		}
	};

}
