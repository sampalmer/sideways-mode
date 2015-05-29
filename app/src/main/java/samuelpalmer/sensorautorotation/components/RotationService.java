package samuelpalmer.sensorautorotation.components;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.ServiceHolder;
import samuelpalmer.sensorautorotation.ServiceRevivalManager;
import samuelpalmer.sensorautorotation.ThisApplication;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationPoster;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationUpdater;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;
import samuelpalmer.sensorautorotation.userinteractivity.UserInteractivityThing;

public class RotationService extends Service {

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceNotificationUpdater serviceNotificationUpdater;
	private UserInteractivityThing userInteractivityThing;

	@Override
	public void onCreate() {
		super.onCreate();

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen();
		ApplicationSettings appSettings = new ApplicationSettings();

		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(appSettings, screen, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screen, calculator);

		serviceNotificationUpdater = new ServiceNotificationUpdater(new ServiceNotificationPoster(this), appSettings, calculator, screen);
		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		serviceNotificationManager = new ServiceNotificationManager(serviceNotificationUpdater, appSettings, screenUpdater);
		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityThing = new UserInteractivityThing(calculator, screenUpdater);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();
		serviceNotificationManager.ensureStarted();
		userInteractivityThing.ensureStarted();
		serviceRevivalManager.ensureStarted();

		//Don't want to expose this until initialisation has finished
		ServiceHolder.started(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		ServiceHolder.stopped();

		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityThing.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		Toast
				.makeText(ThisApplication.context(), serviceNotificationUpdater.mode().autoRotationStateResourceId(), Toast.LENGTH_SHORT)
				.show();
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}

}
