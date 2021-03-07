package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * For testing purposes only. This allows automated tests to override the orientation sensor
 * to simulate changing the device's physical orientation.
 */
public class SensorOverrideReceiver extends BroadcastReceiver {

    private static final String EXTRA_OVERRIDE = "override";

    /**
     * Sends a broadcast to use the given override.
     */
    public static void sendOverride(Context context, SensorOverride override) {
        Intent intent = new Intent(context, SensorOverrideReceiver.class)
                .putExtra(EXTRA_OVERRIDE, override.name());

        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String overrideName = intent.getStringExtra(EXTRA_OVERRIDE);
        if (overrideName == null)
            throw new RuntimeException("No override specified");

        SensorOverride override = SensorOverride.valueOf(overrideName);
        SensorOverrider.setOverride(override);
    }

}
