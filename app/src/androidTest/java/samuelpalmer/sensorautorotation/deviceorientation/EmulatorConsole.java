package samuelpalmer.sensorautorotation.deviceorientation;

import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-level interface to Android emulator Telnet console
 */
public class EmulatorConsole implements AutoCloseable {

    private static String TAG = EmulatorConsole.class.getSimpleName();

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

    private EmulatorConnection connection;

    /*
     * TODO: Ensure the port being used matches the emulator we're running on. There might be multiple emulators running, each with different ports. Or maybe this emulator was launched after another one, and then the other was closed, leaving this emulator as the only one, but still using a non-standard port.
     *  One way to do this is to use the "redir" command to redirect a port from the host to the device, then listen to that port, then send data to it at the host, and confirm it's received in the emulator
     *  Or maybe somehow make use of the "avd name" and "avd id" commands?
     *  Or maybe change one of the sensor values through the emulator, and read the value within the app code
     */

    public EmulatorConsole() throws IOException {
        connection = new EmulatorConnection(HOST_LOOPBACK, HOST_FIRST_EMULATOR_CONSOLE_PORT);

        String[] preamble = connection.runCommand(null);
        checkPreamble(preamble);
    }

    @Override
    public void close() {
        connection.close();
        connection = null;
    }

    private static void checkPreamble(String[] preamble) {
        Log.v(TAG, "Got preamble: " + String.join("\n", preamble));

        if (preamble.length == 0)
            throw new RuntimeException("Emulator preamble is empty");

        String firstLine = preamble[0];
        if (firstLine.equals("Android Console: Authentication required")) {
            String authTokenPath = getAuthTokenPath(preamble);

            if (authTokenPath != null)
                throw new RuntimeException("Emulator requires authentication. Please remove contents of auth token file at " + authTokenPath + ".");
            else {
                Log.w(TAG, "Couldn't determine path to emulator auth token: " + String.join("\n", preamble));
                throw new RuntimeException("Emulator requires authentication. Please disable authentication. Emulator response: " + String.join("\n", preamble));
            }
        }
    }

    private static String getAuthTokenPath(String[] preamble) {
        if (preamble.length >= 2) {
            String authTokenLocationLine = preamble[preamble.length - 2];
            Matcher authTokenLocationMatcher = authTokenLocationPattern.matcher(authTokenLocationLine);

            if (authTokenLocationMatcher.matches())
                return authTokenLocationMatcher.group(1);
        }

        return null;
    }

    private static final Pattern authTokenLocationPattern = Pattern.compile("^'(.+)'$");

    /**
     * Rotate the device clockwise by 90 degrees
     */
    public void rotateClockwise() {
        String[] response = connection.runCommand("rotate");
        validateEmptyResponse(response);
    }

    /**
     * Set acceleration sensor to the given values (in m/s/s)
     */
    public void setAcceleration(float x, float y, float z) {
        String command = String.format("sensor set acceleration %f:%f:%f", x, y, z);
        String[] response = connection.runCommand(command);
        validateEmptyResponse(response);
    }

    private static void validateEmptyResponse(String[] response) {
        if (response.length != 0)
            throw new RuntimeException("Received unexpected response: " + String.join("\n", response));
    }

}
