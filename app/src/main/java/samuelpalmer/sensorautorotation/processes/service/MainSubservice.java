package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationPoster;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityThing;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class MainSubservice {

	public final RotationService service;

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceNotificationUpdater serviceNotificationUpdater;
	private UserInteractivityThing userInteractivityThing;
	private ServiceNotificationPoster poster;

	public MainSubservice(RotationService service) {
		this.service = service;
	}

	public void start() {
		//Expose this straight away in case it's needed to stop the service due to an initialisation failure
		ServiceHolder.started(this);

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen();
		ApplicationSettings appSettings = new ApplicationSettings(Prefs.prefs());

		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(appSettings, screen, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screen, calculator);

		poster = new ServiceNotificationPoster(service);
		serviceNotificationUpdater = new ServiceNotificationUpdater(poster, appSettings, calculator, screen);
		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		serviceNotificationManager = new ServiceNotificationManager(serviceNotificationUpdater, appSettings, screenUpdater);
		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityThing = new UserInteractivityThing(calculator, screenUpdater);

		service.registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();
		serviceNotificationManager.ensureStarted();
		userInteractivityThing.ensureStarted();
		serviceRevivalManager.ensureStarted();
	}

	public void stop() {
		poster.ensureRemoved();
		screenOrientationController.ensureStopped();
		service.unregisterReceiver(shutdownReceiver);
		userInteractivityThing.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();

		ServiceHolder.stopped();
	}

	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			service.stopMainSubservice();
		}
	};

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		CharSequence message = serviceNotificationUpdater.getSummary(serviceNotificationUpdater.mode());
		ToastReceiver.send(message);
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
