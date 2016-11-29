package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.NaturalOrientationDetector;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.Screen;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.service.NotificationUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementMonitor;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplayManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusFactory;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityChangeHandler;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {

	public static final String ACTION_TOGGLE = "toggle";
	public static final String ACTION_ROTATE_LEFT = "rotate_left";
	public static final String ACTION_ROTATE_RIGHT = "rotate_right";

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	ServiceStatusDisplayManager serviceStatusDisplayManager;
	private ServiceRevivalManager serviceRevivalManager;
	ServiceStatusFactory serviceStatusFactory;
	private UserInteractivityChangeHandler userInteractivityChangeHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementMonitor orientationEnforcementMonitor;
	ActivityMonitor activityMonitor;
	private AccessibilityServiceManager accessibilityServiceManager;

	@Override
	public void onCreate() {
		super.onCreate();

		//TODO: Maybe check if the service is actually enabled and stop if it's not?

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen(SystemServices.windowManager());

		applicationState = new ApplicationState(systemSettings);

		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);

		calculator = new OrientationCalculator(screenRotationMonitor, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor, calculator);

		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		LockScreenMonitor lockScreenMonitor = new LockScreenMonitor(CurrentProcess.context());
		serviceStatusFactory = new ServiceStatusFactory(calculator, screenRotationMonitor, new NaturalOrientationDetector(screen), screenUpdater, lockScreenMonitor);
		serviceStatusDisplayManager = new ServiceStatusDisplayManager(serviceStatusFactory);
		serviceStatusDisplayManager.onServiceStatusChanged.add(new NotificationUpdater(this));
		if (Build.VERSION.SDK_INT >= 24)
			serviceStatusDisplayManager.onServiceStatusChanged.add(new QuickSettingStatusDisplay());
		serviceStatusDisplayManager.onServiceStatusChanged.add(new ServiceStatusContentProvider.RemoteStatusDisplay());

		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityChangeHandler = new UserInteractivityChangeHandler(calculator, screenUpdater, screenRotationMonitor);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementMonitor = new OrientationEnforcementMonitor(this, activityMonitor, lockScreenMonitor);
		accessibilityServiceManager = new AccessibilityServiceManager();

		systemAutoRotationMonitor.ensureStarted();
		screenOrientationController.ensureStarted();
		serviceStatusDisplayManager.ensureStarted();
		userInteractivityChangeHandler.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementMonitor.ensureStarted();
		accessibilityServiceManager.ensureStarted();

		//Don't want anything accessing the service 'til it's finished initialising.
		ServiceHolder.started(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (action != null)
				handle(action);
		}

		return Service.START_STICKY;
	}

	public void handle(String action) {
		switch (action) {
			case ACTION_TOGGLE:
				toggleRotation();
				break;
			case ACTION_ROTATE_LEFT:
				calculator.offsetScreenBy(new Rotation(-1));
				break;
			case ACTION_ROTATE_RIGHT:
				calculator.offsetScreenBy(new Rotation(1));
				break;
		}
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

		//Don't want anything accessing the service during deinitialisation.
		ServiceHolder.stopped();

		accessibilityServiceManager.ensureStopped();
		orientationEnforcementMonitor.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityChangeHandler.ensureStopped();
		serviceStatusDisplayManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();
	}

	private void toggleRotation() {
		calculator.setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		CharSequence message = serviceStatusFactory.generate().getSummary();
		ToastReceiver.send(message);
	}

}
