package samuelpalmer.sensorautorotation.rules.deviceorientation;

import android.Manifest;
import android.net.TrafficStats;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Low-level Telnet connection to Android emulator. Takes care of how to talk to the emulator, but
 * not what to tell it to do.
 */
class EmulatorConnection implements AutoCloseable {

    private static String TAG = EmulatorConnection.class.getSimpleName();
    private static final Pattern responseStatusPattern = Pattern.compile("^(?:OK|KO: (.+))$");

    private Socket telnetClient;
    private BufferedReader inputStream;
    private BufferedWriter outputStream;

    public EmulatorConnection(String host, int port) throws IOException {
        Log.v(TAG, "Setting connection timeouts");
        telnetClient = new Socket();
        telnetClient.setSoTimeout(2500);

        Log.v(TAG, "Connecting to emulator");
        TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId()); // Prevents StrictMode from logging an UntaggedSocketViolation on recent Android versions: https://stackoverflow.com/a/57963770/238753

        InetSocketAddress address = new InetSocketAddress(host, port);
        if (address.isUnresolved())
            throw new RuntimeException("Couldn't resolve address: " + address);

        try {
            telnetClient.connect(address, 25); // The connection should be practically instant since it's just from VM to Host. Note that if the emulator is being turned on then it takes an extra 10-15ms.
        } catch (SocketException e) {
            if ("Permission denied".equals(e.getMessage()))
                throw new RuntimeException("Don't have permission to Telnet to emulator. You're probably missing the " + Manifest.permission.INTERNET + " permission.", e);
            else
                throw e;
        }
        Log.v(TAG, "Connected to emulator");

        inputStream = new BufferedReader(new InputStreamReader(telnetClient.getInputStream()));
        outputStream = new BufferedWriter(new OutputStreamWriter(telnetClient.getOutputStream()));
    }

    @Override
    public void close() {
        try {
            // Telling emulator to finish the connection. This is a special command because it has no response.
            sendCommand("exit");

            // Not closing streams since TelnetClient.disconnect() does this for us
            inputStream = null;
            outputStream = null;

            telnetClient.close();;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        telnetClient = null;
    }

    /**
     * Sends the given command to the emulator, validates the result, and returns the response
     * content.
     *
     * @param command the command to send to the emulator, or <c>null</c> to not send a command
     *                and just process a pre-existing response
     */
    public String[] runCommand(@Nullable String command) {
        if (telnetClient == null)
            throw new IllegalStateException("Connection is closed");

        try {
            if (command != null)
                sendCommand(command);

            String[] response = readResponse();
            checkResponseStatus(command, response);
            return getResponseContent(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the given command to the emulator
     */
    private void sendCommand(String command) throws IOException {
        if (command.contains("\n") || command.contains("\r"))
            throw new IllegalArgumentException("Emulator command contains new lines: " + command);

        Log.d(TAG, "Running emulator command: " + command);

        outputStream.write(command + "\n");

        // Must flush what we send to ensure it isn't just waiting in the buffer
        outputStream.flush();
    }

    /**
     * Reads and returns all response lines
     */
    private String[] readResponse() throws IOException {
        // Fetching all response text
        List<String> lines = new ArrayList<>();
        boolean gotStatusLine = false;

        // Checking if we have data to read to ensure readLine() won't block
        while (!gotStatusLine) {
            String line;

            try {
                line = inputStream.readLine();
            } catch (SocketTimeoutException timeout) {
                throw new RuntimeException("Timed out waiting for response text. Text received so far: " + String.join("\n", lines), timeout);
            }

            if (line == null)
                break;

            Log.d(TAG, "Got emulator response: " + line);

            // We need to check for the status line so we know when we've finished reading.
            // Otherwise, we could finish too early if the response text doesn't show up quickly enough.
            // Or we could try to read too far and get stuck waiting for response text.
            if (responseStatusPattern.matcher(line).matches())
                gotStatusLine = true;

            lines.add(line);
        }

        if (lines.isEmpty())
            throw new RuntimeException("Got 0 response lines from emulator");

        return lines.toArray(new String[0]);
    }

    /**
     * Validates that the given response status line is in the expected format and is an "OK" response
     */
    private static void checkResponseStatus(@Nullable String command, String[] response) {
        String statusLine = response[response.length - 1];

        Matcher statusMatcher = responseStatusPattern.matcher(statusLine);
        if (!statusMatcher.matches()) {
            String message = String.format("Response status in unexpected format: %s. Complete response: %s.", statusLine, String.join("\n", response));
            throw new RuntimeException(message);
        }

        String commandError = statusMatcher.group(1);
        if (commandError != null) {
            String message = String.format("Command %s rejected by emulator: %s", command, commandError);
            throw new RuntimeException(message);
        }
    }

    private static String[] getResponseContent(String[] response) {
        // Returning a copy without the last line
        return Arrays.copyOfRange(response, 0, response.length - 1);
    }

}
