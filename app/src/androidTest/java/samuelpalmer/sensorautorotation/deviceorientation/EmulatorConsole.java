package samuelpalmer.sensorautorotation.deviceorientation;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-level interface to Android emulator Telnet console
 */
@SuppressWarnings({"SameParameterValue", "unused", "WeakerAccess"})
class EmulatorConsole implements AutoCloseable {

    private static String TAG = EmulatorConsole.class.getSimpleName();
    private static final Pattern authTokenPathPattern = Pattern.compile("^'(.+)'$");
    private static final Pattern sensorValueLine = Pattern.compile("^(.+) = (.+):(.+):(.+)$");

    private EmulatorConnection connection;

    public EmulatorConsole(String host, int port) throws IOException {
        connection = new EmulatorConnection(host, port);

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
        if (preamble.length >= 1) {
            String authTokenPathLine = preamble[preamble.length - 1];
            Matcher authTokenPathMatcher = authTokenPathPattern.matcher(authTokenPathLine);

            if (authTokenPathMatcher.matches())
                return authTokenPathMatcher.group(1);
        }

        return null;
    }

    /**
     * Rotate the device clockwise by 90 degrees
     */
    public void rotateClockwise() {
        String[] response = connection.runCommand("rotate");
        validateEmptyResponse(response);
    }

    /**
     * Gets current acceleration sensor values (in m/s/s)
     * @return [x, y, z]
     */
    public float[] getAcceleration() {
        return getSensor("acceleration");
    }

    /**
     * Set acceleration sensor to the given values (in m/s/s)
     */
    public void setAcceleration(float x, float y, float z) {
        setSensor("acceleration", x, y, z);
    }

    /**
     * Gets proximity sensor value in centimetres
     */
    public float getProximity() {
        float[] values = getSensor("proximity");
        return values[0];
    }

    /**
     * Sets proximity sensor value in centimetres
     */
    public void setProximity(float distanceCentimetres) {
        setSensor("proximity", distanceCentimetres, null, null);
    }

    @SuppressWarnings("ConstantConditions")
    private float[] getSensor(String sensor) {
        // Run command
        String command = "sensor get " + sensor;
        String[] response = connection.runCommand(command);
        String responseLine = validateSingleLineResponse(response);

        // Parse result
        Matcher matcher = sensorValueLine.matcher(responseLine);
        if (!matcher.matches())
            throw new RuntimeException("Sensor response in unexpected format: " + responseLine);

        // Validate sensor name
        String responseSensor = matcher.group(1);
        if (!sensor.equals(responseSensor)) {
            String message = String.format("Response sensor %s is different to requested sensor %s: %s", responseSensor, sensor, responseLine);
            throw new RuntimeException(message);
        }

        // Get sensor values
        float a = Float.parseFloat(matcher.group(2));
        float b = Float.parseFloat(matcher.group(3));
        float c = Float.parseFloat(matcher.group(4));

        return new float[]{a, b, c};
    }

    private void setSensor(String sensor, float a, @Nullable Float b, @Nullable Float c) {
        String command = String.format("sensor set %s %g", sensor, a);
        if (b != null) {
            command += String.format(":%g", b);

            if (c != null)
                command += String.format(":%g", c);
        }

        String[] response = connection.runCommand(command);
        validateEmptyResponse(response);
    }

    /**
     * Add a new port redirection. As an example, <c>redir tcp:5000:6000</c> will allow any packets
     * sent to the host's TCP port 5000 to be routed to TCP port 6000 of the emulated device.
     *
     * @param protocol either 'tcp' or 'udp'
     * @param hostPort a number indicating which port on the host to open
     * @param guestPort a number indicating which port to route to on the device
     */
    public void addPortRedirection(String protocol, int hostPort, int guestPort) {
        String command = String.format("redir add %s:%d:%d", protocol, hostPort, guestPort);
        String[] response = connection.runCommand(command);
        validateEmptyResponse(response);
    }

    /**
     * Remove a port redirecion that was created with {@link #addPortRedirection(String, int, int)}
     */
    public void deletePortRedirection(String protocol, int hostPort) {
        String command = String.format("redir del %s:%d", protocol, hostPort);
        String[] response = connection.runCommand(command);
        validateEmptyResponse(response);
    }

    private static void validateEmptyResponse(String[] response) {
        if (response.length != 0)
            throw new RuntimeException("Received unexpected response: " + String.join("\n", response));
    }

    private String validateSingleLineResponse(String[] response) {
        if (response.length != 1) {
            String message = String.format("Expected single line response but got %d lines: %s", response.length, String.join("\n", response));
            throw new RuntimeException(message);
        }

        return response[0];
    }

}
