package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.util.Log;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

/**
 * Keeps track of the current state of the sensor override. For testing purposes only.
 */
final class SensorOverrider {

    private static String TAG = SensorOverrider.class.getSimpleName();

    private static SensorOverride currentOverride = SensorOverride.OFF;
    private static UpdatingEventRegistrar overrideChanged = new UpdatingEventRegistrar();

    public static void setOverride(SensorOverride override) {
        Log.i(TAG, "Setting sensor override to " + override.name());

        currentOverride = override;
        overrideChanged.report();
    }

    public static EventRegistrar onOverrideChanged() {
        return overrideChanged;
    }

    public static SensorOverride getOverride() {
        return currentOverride;
    }

    private SensorOverrider() {}

}
