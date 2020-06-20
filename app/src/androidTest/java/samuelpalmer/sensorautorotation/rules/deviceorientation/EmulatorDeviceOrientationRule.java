package samuelpalmer.sensorautorotation.rules.deviceorientation;

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
        setUpright();
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
    public void setUpright() {
        console.setAcceleration(0, 9.81f, 0);
    }

    @Override
    public void setRightSide() {
        console.setAcceleration(-9.81f, 0, 0);
    }

    @Override
    public void setUpsideDown() {
        console.setAcceleration(0, -9.81f, 0);
    }

    @Override
    public void setLeftSide() {
        console.setAcceleration(9.81f, 0, 0);
    }

    @Override
    public void setUnknown() {
        console.setAcceleration(0, 0, 9.81f);
    }

}
