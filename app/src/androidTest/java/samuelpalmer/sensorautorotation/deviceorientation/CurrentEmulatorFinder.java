package samuelpalmer.sensorautorotation.deviceorientation;

import android.hardware.Sensor;
import android.util.Log;

import java.io.IOException;

/**
 * Unfortunately, we don't know which port the current emulator's Telnet server is listening on.
 * There might be multiple emulators running, each with different ports.
 * Or maybe this emulator was launched after another one, and then the other was closed, leaving this emulator as the only one, but still using a non-default port.
 */
class CurrentEmulatorFinder {

    private static String TAG = CurrentEmulatorFinder.class.getSimpleName();

    /**
     * IP address of your development machine that is running the emulator.
     * See <a href="https://developer.android.com/studio/run/emulator-networking.html">Set up Android Emulator networking</a> for more info.
     */
    private static final String HOST_LOOPBACK = "10.0.2.2";

    /**
     * TCP port that emulator is listening on within your development machine.
     * See <a href="https://developer.android.com/studio/run/emulator-networking.html">Set up Android Emulator networking</a> for more info.
     */
    private static final int HOST_FIRST_EMULATOR_CONSOLE_PORT = 5554;

    /**
     * Maximum number of emulators to look for.
     */
    private static final int MAX_DEVICES = 10;

    public static EmulatorConsole getCurrentEmulator() {
        for (int i = 0; i < MAX_DEVICES; ++i) {
            // For some reason, the port number is incremented by 2 for each new emulator instance
            int port = HOST_FIRST_EMULATOR_CONSOLE_PORT + i * 2;
            Log.v(TAG, "Looking for emulator at " + HOST_LOOPBACK + ":" + port);

            EmulatorConsole emulator;
            try {
                emulator = new EmulatorConsole(HOST_LOOPBACK, port);
            } catch (IOException e) {
                Log.v(TAG, "Couldn't find emulator at " + HOST_LOOPBACK + ":" + port, e);
                continue;
            }

            if (isThisEmulator(emulator))
                return emulator;

            emulator.close();
        }

        throw new RuntimeException("Couldn't find current emulator");
    }

    private static boolean isThisEmulator(EmulatorConsole emulator) {
        // Setting the proximity sensor through the emulator console to see if it changes the current device

        float emulatorOriginalProximity = emulator.getProximity();

        // Using integers to ensure we can do an exact match
        // The emulator truncates values with more than 6 significant digits
        float emulatorProximity = (float) Math.floor(Math.random() * 999999);
        emulator.setProximity(emulatorProximity);

        float[] sensorValues = SensorPoller.pollSensor(Sensor.TYPE_PROXIMITY);
        emulator.setProximity(emulatorOriginalProximity);

        float deviceProximity = sensorValues[0];
        if (emulatorProximity != deviceProximity) {
            Log.v(TAG, "Emulator proximity is " + emulatorProximity + ", but current device proximity is " + deviceProximity);
            return false;
        }

        return true;
    }

    private CurrentEmulatorFinder() {}

}
