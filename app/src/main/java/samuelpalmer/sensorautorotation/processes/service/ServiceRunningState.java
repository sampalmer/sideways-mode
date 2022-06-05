package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.IncompatibleAppsManager;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.DeviceInteractivityMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceRunningState extends State {
    
    private static final String TAG = ServiceRunningState.class.getSimpleName();
    private static WeakReference<Context> cachedWindowContextReference;

    private final Context context;
    private final OrientationCalculator calculator;
    final RunningStateUpdater runningStateUpdater;
    private final LowMemoryDetector lowMemoryDetector;
    private final OrientationSensorHandler orientationSensorHandler;
    private final SystemAutoRotationMonitor systemAutoRotationMonitor;
    private final IncompatibleAppsManager incompatibleAppsManager;
    private final ScreenOrientationUpdater screenUpdater;
    private final AppDisabler appDisabler;
    private final SideSwapHandler sideSwapHandler;
    private final DeviceInteractivityMonitor deviceInteractivityMonitor;

    public ServiceRunningState(RotationService service) {
        this.context = makeContext(service);

        // This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
        EnvironmentErrorManager.clear(context);

        ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(context);
        calculator = new OrientationCalculator(context, screenRotationMonitor);
        screenUpdater = new ScreenOrientationUpdater(context, calculator, screenRotationMonitor);
        runningStateUpdater = new RunningStateUpdater(context, calculator, service);
        lowMemoryDetector = new LowMemoryDetector(context);
        orientationSensorHandler = new OrientationSensorHandler(context, calculator, screenRotationMonitor);
        systemAutoRotationMonitor = new SystemAutoRotationMonitor(context);
        ActivityMonitor activityMonitor = ActivityMonitor.create(context);
        incompatibleAppsManager = new IncompatibleAppsManager(context, activityMonitor);
        appDisabler = new AppDisabler(context);
        sideSwapHandler = new SideSwapHandler(context, calculator);
        deviceInteractivityMonitor = new DeviceInteractivityMonitor(context);
    }

    /**
     * Creates a context that is also suitable for UI-related work on the default display.
     *
     * Since we're in a service, then our context doesn't have UI-related information, such as
     * {@link android.content.res.Configuration}, which is needed for correct resource resolution.
     *
     * As of Android R, this is required when accessing {@link WindowManager} etc.
     */
    private static Context makeContext(RotationService service) {
        if (Build.VERSION.SDK_INT >= 30) {
            // The OS restricts the rate at which window contexts may be created, which can lead to
            // the following exception when the user or an automated test repeatedly starts and
            // stops the app:
            // java.lang.UnsupportedOperationException: createWindowContext failed! Too many unused window contexts. Please see Context#createWindowContext documentation for detail.
            //
            // So, unfortunately, we need to cache the window context and re-use it

            Context cachedContext = cachedWindowContextReference == null ? null : cachedWindowContextReference.get();
            if (cachedContext == null) {
                // Using the app context since we will cache the result beyond the lifetime of the
                // service
                Context appContext = service.getApplicationContext();
                Context displayContext = makeDisplayContext(appContext);
                cachedContext = displayContext.createWindowContext(ScreenOrientationEnforcer.getWindowType(), null);
                cachedWindowContextReference = new WeakReference<>(cachedContext);
            }

            return cachedContext;
        } else if (Build.VERSION.SDK_INT >= 17) {
            return makeDisplayContext(service);
        } else
            return service;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static Context makeDisplayContext(Context service) {
        DisplayManager displayManager = ContextCompat.getSystemService(service, DisplayManager.class);
        assert displayManager != null;
        Display primaryDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        return service.createDisplayContext(primaryDisplay);
    }

    @Override
    protected void start() {
        Log.i(TAG, "App is turning on");

        systemAutoRotationMonitor.ensureStarted();
        runningStateUpdater.ensureStarted();
        screenUpdater.ensureStarted();
        lowMemoryDetector.ensureStarted();
        incompatibleAppsManager.ensureStarted();
        appDisabler.ensureStarted();
        sideSwapHandler.ensureStarted();

        deviceInteractivityMonitor.subscribe(deviceInteractivityListener);
        deviceInteractivityListener.update();
        
        ServiceRunningStateHolder.serviceStarted(this);
    }
    
    @Override
    protected void stop() {
        Log.i(TAG, "App is turning off");
        
        ServiceRunningStateHolder.serviceStopped();
        
        deviceInteractivityMonitor.unsubscribe(deviceInteractivityListener);
        sideSwapHandler.ensureStopped();
        appDisabler.ensureStopped();
        orientationSensorHandler.ensureStopped();
        incompatibleAppsManager.ensureStopped();
        systemAutoRotationMonitor.ensureStopped();
        screenUpdater.ensureStopped();
        runningStateUpdater.ensureStopped();
        lowMemoryDetector.ensureStopped();
        
        ServiceSettings
            .getSharedPreferences(context)
            .edit()
            .remove(ServiceSettings.KEY_PERSISTENT_STATE)
            .apply();
    }
    
    private final EventHandler deviceInteractivityListener = new EventHandler() {
        @Override
        public void update() {
            if (deviceInteractivityMonitor.isInteractive() != orientationSensorHandler.isStarted()) {
                // We should only monitor the sensors while the screen is on. Otherwise it will waste battery.
                if (deviceInteractivityMonitor.isInteractive()) {
                    Log.i(TAG, "Device is interactive. Listening to sensors.");
                    orientationSensorHandler.ensureStarted();
                } else {
                    /*
                    Don't want to turn the app off when the screen turns off because the screen might
                    have turned off due to inactivity while the user is actually still using the device.
                     */
                    Log.i(TAG, "Device is not interactive. Not listening to sensors.");
                    orientationSensorHandler.ensureStopped();
                }
            }
        }
    };
    
    public void setSide(UserSide side) {
        calculator.setSide(side);
    }
    
}
