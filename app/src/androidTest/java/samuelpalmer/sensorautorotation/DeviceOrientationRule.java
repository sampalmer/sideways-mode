package samuelpalmer.sensorautorotation;

import org.junit.rules.ExternalResource;

public class DeviceOrientationRule extends ExternalResource {

    private EmulatorConsole console;

    @Override
    protected void before() throws Throwable {
        super.before();

        // TODO: Detect if the device is an emulator before proceeding with Telnet commands
        console = new EmulatorConsole();
        // TODO: After initialising the connection to the emulator, run a "rotate" followed by a "sensor set acceleration 0:9.81:0" to work around the issue where the emulator window on the host doesn't rotate when the sensor is changed
    }

    @Override
    protected void after() {
        super.after();

        console.close();
        console = null;
    }

}
