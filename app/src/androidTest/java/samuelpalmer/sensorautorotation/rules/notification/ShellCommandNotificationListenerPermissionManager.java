package samuelpalmer.sensorautorotation.rules.notification;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;

/**
 * Manages permissions using the {@code cmd notification} shell command
 */
@RequiresApi(api = Build.VERSION_CODES.O_MR1)
class ShellCommandNotificationListenerPermissionManager extends NotificationListenerPermissionManager {

    private final String listenerComponentString;

    ShellCommandNotificationListenerPermissionManager() {
        super();
        listenerComponentString = listenerComponent.flattenToString();
    }

    @Override
    public void grantAccess() {
        String cmd = "cmd notification allow_listener " + listenerComponentString;

        try {
            device.executeShellCommand(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void revokeAccess() {
        String cmd = "cmd notification disallow_listener " + listenerComponentString;

        try {
            device.executeShellCommand(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
