package samuelpalmer.sensorautorotation.rules;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverride;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverrideReceiver;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Simulates device orientation by directing the target app to ignore the real sensors and instead
 * look at the override values supplied here.
 */
public class MockDeviceOrientationRule extends ExternalResource {

    private Context context;

    @Override
    protected void before() throws Throwable {
        super.before();
        // Must use target context to send the broadcasts. Otherwise they won't make it to the receiver.
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        setRotation(Orientation.NATURAL);
    }

    @Override
    protected void after() {
        super.after();
        SensorOverrideReceiver.sendOverride(context, SensorOverride.OFF);
        context = null;
    }

    public void setRotation(@Nullable Orientation deviceOrientation) {
        if (deviceOrientation == null) {
            SensorOverrideReceiver.sendOverride(context, SensorOverride.UNKNOWN);
            return;
        }

        switch (deviceOrientation) {
            case NATURAL:
                SensorOverrideReceiver.sendOverride(context, SensorOverride.UPRIGHT);
                return;
            case RIGHT_SIDE:
                SensorOverrideReceiver.sendOverride(context, SensorOverride.RIGHT_SIDE);
                return;
            case UPSIDE_DOWN:
                SensorOverrideReceiver.sendOverride(context, SensorOverride.UPSIDE_DOWN);
                return;
            case LEFT_SIDE:
                SensorOverrideReceiver.sendOverride(context, SensorOverride.LEFT_SIDE);
                return;
            default:
                throw new IllegalArgumentException("Unknown orientation: " + deviceOrientation);
        }
    }

}
