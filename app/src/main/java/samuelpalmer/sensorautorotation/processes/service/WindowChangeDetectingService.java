package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.AccessibilityServiceActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.utilities.ObjectExtensions;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarComponent;

/**
 * {@link AccessibilityService} used to implement {@link AccessibilityServiceActivityMonitor}
 */
// Keep in mind that renaming (or moving?) this class will result in users having to re-enable the accessibility service.
public class WindowChangeDetectingService extends AccessibilityService {

    private static final String TAG = WindowChangeDetectingService.class.getSimpleName();

    public static EventRegistrar onActivityChange() { return onActivityChange; }
    private static final EventRegistrarComponent onActivityChange = new EventRegistrarComponent();
    private static ComponentName currentActivity;
    public static ComponentName getCurrentActivity() { return currentActivity; }

    private static void setCurrentActivity(ComponentName newCurrentActivity) {
        if (!ObjectExtensions.areEqual(currentActivity, newCurrentActivity)) {
            currentActivity = newCurrentActivity;
            onActivityChange.report();
        }
    }

    private boolean wasListening = false;
    
    @Override
    public void onCreate() {
        super.onCreate();

        onActivityChange.onSubscriptionsChange.subscribe(subscriptionsChanged);

        Log.i(TAG, "Accessibility service created");
    }

    // So far, this only gets called when the user disables the service in the accessibility settings.
    // Stopping the service with stopService() doesn't trigger this, presumably because something has bound to it.
    @Override
    public void onDestroy() {
        super.onDestroy();

        onActivityChange.onSubscriptionsChange.unsubscribe(subscriptionsChanged);

        Log.i(TAG, "Accessibility service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Accessibility service start command received");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Accessibility service connected");
        updateServiceInfo();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // In previous versions of this app, all devices used the accessibility service.
            // Now, however, most devices use a different technique.
            // So we might as well turn off the accessibility service if it isn't needed to save resources.

            ActivityMonitor activityMonitor = ActivityMonitor.create(this);

            if (!(activityMonitor instanceof AccessibilityServiceActivityMonitor)) {
                disableSelf();
                Log.i(TAG, "Turned off accessibility service since it's not needed.");
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Accessibility service disconnected");
        setCurrentActivity(null); // We're no longer listening for events, so now we're in the dark about which activity is on top
        return super.onUnbind(intent);
    }

    private final EventHandler subscriptionsChanged = this::updateServiceInfo;

    /**
     * Prevents accessibility events from being received when the application isn't running.
     * This is an attempt to reduce unnecessary performance impact.
     */
    private void updateServiceInfo() {
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();

        if (onActivityChange.hasSubscriptions()) {
            config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

            // Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        }
        
        setServiceInfo(config);
        Log.i(TAG, "Accessibility service " + ((config.eventTypes == 0) ? "un" : "") + "subscribed for events");
        
        if (onActivityChange.hasSubscriptions() && !wasListening) {
            // If we're just starting up, we'll launch a self-closing activity so we can detect which activity the user initially has open.
            Intent intent = new Intent(this, WaitForActivityDetectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // <!-- DO NOT use FLAG_ACTIVITY_NO_ANIMATION, since that causes a black flicker when the activity starts or finishes on API 27. -->
            startActivity(intent);
        }
        
        wasListening = onActivityChange.hasSubscriptions();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (onActivityChange.hasSubscriptions() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (BuildConfig.DEBUG)
                // This can be useful for development, but we skip this in Production since it could be a privacy violation.
                Log.d(getClass().getSimpleName(), "Window state changed: " + event.getPackageName() + "/" + event.getClassName());
            
            ComponentName componentName = ComponentNameExtensions.tryMakeFrom(event);
            if (componentName != null && isActivity(componentName))
                setCurrentActivity(componentName);
        }
    }

    private boolean isActivity(ComponentName componentName) {
        // I'm using exceptions for control flow here because I didn't find a faster approach
        try {
            // It'd be nice to optimise this away, but the only other option I can think of is caching the results, which could waste RAM...
            getPackageManager().getActivityInfo(componentName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public void onInterrupt() {}

}