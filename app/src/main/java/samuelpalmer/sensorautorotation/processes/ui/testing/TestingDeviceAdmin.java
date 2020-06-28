package samuelpalmer.sensorautorotation.processes.ui.testing;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

/**
 * Enables use of Android's Device Administration API for testing purposes
 */
public class TestingDeviceAdmin extends DeviceAdminReceiver {

    public static final UpdatingEventRegistrar onEnabled = new UpdatingEventRegistrar();

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        onEnabled.report();
    }

}
