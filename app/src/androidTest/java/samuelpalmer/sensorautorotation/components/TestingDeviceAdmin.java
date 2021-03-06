package samuelpalmer.sensorautorotation.components;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

/**
 * Enables use of Android's Device Administration API for testing purposes
 */
public class TestingDeviceAdmin extends DeviceAdminReceiver {

    public static final UpdatingEventRegistrar onEnabled = new UpdatingEventRegistrar();

    private static boolean isEnabled = false;

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        isEnabled = true;
        onEnabled.report();
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        isEnabled = false;
    }

    @MainThread
    public static boolean isEnabled() {
        return isEnabled;
    }

}
