package samuelpalmer.sensorautorotation.deviceorientation;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverride;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverrideReceiver;

/**
 * Simulates device orientation by directing the target app to ignore the real sensors and instead
 * look at the override values supplied here.
 */
class MockDeviceOrientationRule extends DeviceOrientationRule {

    private Context context;

    @Override
    protected void before() throws Throwable {
        super.before();
        context = InstrumentationRegistry.getInstrumentation().getContext();
        setUpright();
    }

    @Override
    protected void after() {
        super.after();
        // TODO: Make this block until the command has been processed by the target app. Otherwise our test gets terminated before the broadcast is properly sent, resulting in this not taking effect
        SensorOverrideReceiver.sendOverride(context, SensorOverride.OFF);
        context = null;
    }

    @Override
    public void setUpright() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.UPRIGHT);
    }

    @Override
    public void setRightSide() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.RIGHT_SIDE);
    }

    @Override
    public void setUpsideDown() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.UPSIDE_DOWN);
    }

    @Override
    public void setLeftSide() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.LEFT_SIDE);
    }

    @Override
    public void setUnknown() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.UNKNOWN);
    }

}
