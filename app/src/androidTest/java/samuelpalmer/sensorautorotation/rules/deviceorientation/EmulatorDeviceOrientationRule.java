package samuelpalmer.sensorautorotation.rules.deviceorientation;

import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Controls device orientation by rotating the emulated device itself. This makes it easy to
 * develop and maintain tests since we can actually watch the device rotate as the tests run.
 */
class EmulatorDeviceOrientationRule extends DeviceOrientationRule {

    private EmulatorConsole console;
    private float[] originalAcceleration;

    @Override
    protected void before() throws Throwable {
        super.before();

        console = CurrentEmulatorFinder.getCurrentEmulator();
        originalAcceleration = console.getAcceleration();

        // Older emulators don't rotate the device window on the host in response to acceleration changes, unless you first use the "rotate" command
        // So we'll do this up-front to make sure everything works after we've initialised
        console.rotateClockwise();
        setRotation(Orientation.NATURAL);
    }

    @Override
    protected void after() {
        super.after();

        console.setAcceleration(originalAcceleration[0], originalAcceleration[1], originalAcceleration[2]);
        originalAcceleration = null;

        console.close();
        console = null;
    }

    @Override
    public void setRotation(@Nullable Orientation deviceOrientation) {
        if (deviceOrientation == null) {
            console.setAcceleration(0, 0, 9.81f);
            return;
        }

        switch (deviceOrientation) {
            case NATURAL:
                console.setAcceleration(0, 9.81f, 0);
                return;
            case RIGHT_SIDE:
                console.setAcceleration(-9.81f, 0, 0);
                return;
            case UPSIDE_DOWN:
                console.setAcceleration(0, -9.81f, 0);
                return;
            case LEFT_SIDE:
                console.setAcceleration(9.81f, 0, 0);
                return;
            default:
                throw new IllegalArgumentException("Unknown orientation: " + deviceOrientation);
        }
    }

}
