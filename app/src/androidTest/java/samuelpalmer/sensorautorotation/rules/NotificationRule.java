package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.components.TestingNotificationListener;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;

public class NotificationRule extends ExternalResource {

    private final ActivityScenarioRule<? extends AppCompatActivity> activity;

    private UiDevice device;
    private Context targetContext;
    private ComponentName listenerComponent;
    private String listenerComponentString;

    public NotificationRule(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        this.activity = activity;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        targetContext = instrumentation.getTargetContext();
        listenerComponent = new ComponentName(targetContext, TestingNotificationListener.class);
        listenerComponentString = listenerComponent.flattenToString();

        if (Build.VERSION.SDK_INT >= 27) {
            String cmd = "cmd notification allow_listener " + listenerComponentString;
            device.executeShellCommand(cmd);
        } else if (Build.VERSION.SDK_INT >= 21) {
            Set<ComponentName> originalListeners = getEnabledListeners();
            HashSet<ComponentName> newListeners = new HashSet<>(originalListeners);
            newListeners.add(listenerComponent);
            setEnabledListeners(newListeners);
        } else {
            Intent notificationListenersIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            activity.getScenario().onActivity(a -> a.startActivity(notificationListenersIntent));

            try {
                UiObject2 listenerItem = device.findObject(By
                        .pkg("com.android.settings")
                        .res("com.android.settings", "name")
                        .text("Sideways Mode Testing Notification Listener")
                );
                if (listenerItem == null)
                    throw new RuntimeException("Couldn't find notification listener in list");

                listenerItem.click();

                // TODO: Don't look for the ok button if it's already enabled
                UiObject2 okButton = device.wait(Until.findObject(By.text("OK")), 1000);
                if (okButton == null)
                    throw new RuntimeException("Couldn't find ok button to enable notification listener");

                okButton.click();

                // TODO: Wait for listener to become enabled (see device admin code)
            } finally {
                activity.getScenario().onActivity(ActivityUtils::finishActivitiesOnTopOf);
            }
        }

        if (!isEnabled())
            throw new RuntimeException("Notification listener is not enabled");
    }

    @Override
    protected void after() {
        super.after();

        if (Build.VERSION.SDK_INT >= 27) {
            String cmd = "cmd notification disallow_listener " + listenerComponentString;
            try {
                device.executeShellCommand(cmd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            Set<ComponentName> originalListeners = getEnabledListeners();
            HashSet<ComponentName> newListeners = new HashSet<>(originalListeners);
            newListeners.remove(listenerComponent);
            setEnabledListeners(newListeners);
        }

        // TODO: Implement this for API 18-19

        if (isEnabled())
            throw new RuntimeException("Notification listener is enabled");

        targetContext = null;
        device = null;
        // TODO: Remove notification listener access
    }

    private Set<ComponentName> getEnabledListeners() {
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

    private void setEnabledListeners(Set<ComponentName> newListeners) {
        HashSet<String> flattenedListeners = new HashSet<>(newListeners.size());

        for (ComponentName listener : newListeners) {
            String flattenedListener = listener == null ? "" : listener.flattenToString();
            flattenedListeners.add(flattenedListener);
        }

        String enabledServicesSetting = String.join(":", flattenedListeners);

        try {
            if (enabledServicesSetting.isEmpty())
                device.executeShellCommand("settings delete secure enabled_notification_listeners");
            else
                device.executeShellCommand("settings put secure enabled_notification_listeners " + enabledServicesSetting);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEnabled() {
        Set<ComponentName> enabledListeners = getEnabledListeners();
        return enabledListeners.contains(listenerComponent);
    }

}
