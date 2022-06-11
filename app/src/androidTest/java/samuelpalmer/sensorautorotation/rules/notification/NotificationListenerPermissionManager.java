package samuelpalmer.sensorautorotation.rules.notification;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.components.TestingNotificationListener;

/**
 * Enables, disables, and checks state of {@link samuelpalmer.sensorautorotation.components.TestingNotificationListener}
 */
abstract class NotificationListenerPermissionManager {

    final UiDevice device;
    final ComponentName listenerComponent;
    final Context targetContext;

    public static NotificationListenerPermissionManager create(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        if (Build.VERSION.SDK_INT >= 27)
            return new ShellCommandNotificationListenerPermissionManager();
        else if (Build.VERSION.SDK_INT >= 21)
            return new ShellSettingsNotificationListenerPermissionManager();
        else
            return new UiAutomationNotificationListenerPermissionManager(activity);
    }

    NotificationListenerPermissionManager() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        targetContext = instrumentation.getTargetContext();
        listenerComponent = new ComponentName(targetContext, TestingNotificationListener.class);
    }

    public boolean isEnabled() {
        Set<ComponentName> enabledListeners = getEnabledListeners();
        return enabledListeners.contains(listenerComponent);
    }

    public abstract void grantAccess();
    public abstract void revokeAccess();

    Set<ComponentName> getEnabledListeners() {
        String enabledServicesSetting = Settings.Secure.getString(targetContext.getContentResolver(), "enabled_notification_listeners");
        if (enabledServicesSetting == null)
            return Collections.emptySet();

        String[] flattenedResults = enabledServicesSetting.split(":");
        HashSet<ComponentName> results = new HashSet<>(flattenedResults.length);

        for (String flattenedResult : flattenedResults) {
            ComponentName result = ComponentName.unflattenFromString(flattenedResult);
            results.add(result);
        }

        return results;
    }

    void waitForListenerPermission(boolean targetState) {
        long timeout = System.currentTimeMillis() + 10000;
        while (isEnabled() != targetState) {
            if (System.currentTimeMillis() >= timeout)
                throw new RuntimeException("Took too long for permission state to reach " + targetState);

            try {
                //noinspection BusyWait
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
