package samuelpalmer.sensorautorotation.notifications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final Context context;
	private final ServiceNotification serviceNotification;
	private final OrientationCalculator calculator;
	private final LockScreenHider notificationHider;
	private final EventRegistrar<Void> onShowNotificationAtTopChanged;
	public ServiceNotificationManager(Context context, ServiceNotification serviceNotification, ApplicationSettings settings, OrientationCalculator calculator) {
		this.context = context;
		this.serviceNotification = serviceNotification;
		this.calculator = calculator;
		notificationHider = new LockScreenHider(context, serviceNotification);
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
	}

	@Override
	protected void start() {
		serviceNotification.set();
		notificationHider.ensureStarted();
		onShowNotificationAtTopChanged.subscribe(updateNotification);
		calculator.onChange().subscribe(updateNotification);
		context.registerReceiver(screenRotationMightHaveChanged, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(screenRotationMightHaveChanged);
		calculator.onChange().unsubscribe(updateNotification);
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
