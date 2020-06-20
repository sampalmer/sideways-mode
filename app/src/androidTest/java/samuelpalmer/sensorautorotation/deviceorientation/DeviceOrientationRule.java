package samuelpalmer.sensorautorotation.deviceorientation;

import org.junit.rules.ExternalResource;

public class DeviceOrientationRule extends ExternalResource {

    private EmulatorConsole console;
    private float[] originalAcceleration;

    @Override
    protected void before() throws Throwable {
        super.before();

        // TODO: Detect if the device is an emulator before proceeding with Telnet commands
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

    public void setUpright() {
        console.setAcceleration(0, 9.81f, 0);
    }

    public void setRightSide() {
        console.setAcceleration(-9.81f, 0, 0);
    }

    public void setUpsideDown() {
        console.setAcceleration(0, -9.81f, 0);
    }

    public void setLeftSide() {
        console.setAcceleration(9.81f, 0, 0);
    }

    public void setUnknown() {
        console.setAcceleration(0, 0, 9.81f);
    }

}
