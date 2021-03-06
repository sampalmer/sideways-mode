package samuelpalmer.sensorautorotation.rules.notification;

import android.content.ComponentName;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages permissions using the {@code settings} shell command
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ShellSettingsNotificationListenerPermissionManager extends NotificationListenerPermissionManager {

    public ShellSettingsNotificationListenerPermissionManager() {
        super();
    }

    @Override
    public void grantAccess() {
        Set<ComponentName> originalListeners = getEnabledListeners();
        HashSet<ComponentName> newListeners = new HashSet<>(originalListeners);
        newListeners.add(listenerComponent);
        setEnabledListeners(newListeners);
        waitForListenerPermission(true);
    }

    @Override
    public void revokeAccess() {
        Set<ComponentName> originalListeners = getEnabledListeners();
        HashSet<ComponentName> newListeners = new HashSet<>(originalListeners);
        newListeners.remove(listenerComponent);
        setEnabledListeners(newListeners);
        waitForListenerPermission(false);
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

}
