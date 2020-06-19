package samuelpalmer.sensorautorotation;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmulatorConsole {

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

    /*
     * TODO: Ensure the port being used matches the emulator we're running on. There might be multiple emulators running, each with different ports. Or maybe this emulator was launched after another one, and then the other was closed, leaving this emulator as the only one, but still using a non-standard port.
     *  One way to do this is to use the "redir" command to redirect a port from the host to the device, then listen to that port, then send data to it at the host, and confirm it's received in the emulator
     *  Or maybe somehow make use of the "avd name" and "avd id" commands?
     */

    private UiDevice device;

    public EmulatorConsole() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public void close() {
        device = null;
    }

    /**
     * Rotate the device clockwise by 90 degrees
     */
    public void rotateClockwise() {
        runEmulatorCommand("rotate");
    }

    /**
     * Set acceleration sensor to the given values (in m/s/s)
     */
    public void setAcceleration(float x, float y, float z) {
        String command = String.format("sensor set acceleration %f:%f:%f", x, y, z);
        runEmulatorCommand(command);
    }

    private static final Pattern expectedResponsePattern = Pattern.compile("^(.*)\r\nOK\r\n(.*\r\n)?(?:OK|KO: (.+))\r\n$", Pattern.DOTALL);
    private static final Pattern authPreamblePattern = Pattern.compile("Android Console: Authentication required\r\nAndroid Console: type 'auth <auth_token>' to authenticate\r\nAndroid Console: you can find your <auth_token> in \r\n'(.+)'");
    
    private String runEmulatorCommand(String emulatorCommand) {
        if (emulatorCommand.contains("\n") || emulatorCommand.contains("\r"))
            throw new IllegalArgumentException("Emulator command contains new lines: " + emulatorCommand);

        Log.d(TAG, "Running emulator command: " + emulatorCommand);

        // We must exit the session so the emulator doesn't permanently freeze up waiting for nc to complete
        String emulatorScript = String.format("%s\n%s\n", emulatorCommand, "exit");

        String emulatorResponse;

        // Cannot directly Telnet to the host because this test app doesn't have Internet permission, and we can't grant this permission without also granting it to the target app, which would skew its behaviour: https://stackoverflow.com/questions/14142857/how-can-i-add-additional-android-permissions-for-my-ui-test-project-only
        Log.v(TAG, "Writing emulator script to temp file: " + emulatorScript);
        try (TempTextFile emulatorCommandFile = new TempTextFile("emulatorcommand", ".txt", emulatorScript)) {
            String connectCommand = String.format("cat %s | nc -w 5 %s %d", emulatorCommandFile.getAbsolutePath(), HOST_LOOPBACK, HOST_FIRST_EMULATOR_CONSOLE_PORT);

            Log.v(TAG, "Writing emulator connection command to temp file: " + connectCommand);
            try (TempTextFile connectCommandFile = new TempTextFile("connect", ".sh", connectCommand)) {
                // executeShellCommand() doesn't support piping: https://stackoverflow.com/questions/5928225/how-to-make-pipes-work-with-runtime-exec
                // See also https://stackoverflow.com/a/5928316/238753
                // So we need to have sh do the piping instead
                String startProcessCommand = "/system/bin/sh " + connectCommandFile.getAbsolutePath();

                Log.v(TAG, "Starting process: " + startProcessCommand);
                emulatorResponse = device.executeShellCommand(startProcessCommand);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.v(TAG, "Got response from emulator: " + emulatorResponse);

        Matcher responseMatcher = expectedResponsePattern.matcher(emulatorResponse);
        if (!responseMatcher.matches())
            throw new RuntimeException("Emulator response not in expected format: " + emulatorResponse);

        String preamble = responseMatcher.group(1);
        assert preamble != null;
        Log.v(TAG, "Emulator preamble: " + preamble);

        String commandResponse = responseMatcher.group(2);
        Log.v(TAG, "Emulator command response: " + commandResponse);

        String commandError = responseMatcher.group(3);
        Log.v(TAG, "Emulator command error: " + commandError);

        Matcher authPreambleMatcher = authPreamblePattern.matcher(preamble);
        if (authPreambleMatcher.matches()) {
            String authTokenPath = authPreambleMatcher.group(1);
            throw new RuntimeException("Emulator requires authentication. Please remove contents of auth token file at " + authTokenPath + ".");
        }

        if (!preamble.equals("Android Console: type 'help' for a list of commands"))
            throw new RuntimeException("Emulator preamble not in expected format: " + preamble);

        if (commandError != null)
            throw new RuntimeException("Command rejected by emulator: " + commandError);

        Log.d(TAG, "Emulator responded with: " + commandResponse);

        return commandResponse;
    }

    private static class TempTextFile implements AutoCloseable {

        private File file;

        private TempTextFile(String prefix, String suffix, String contents) throws IOException {
            file = File.createTempFile(prefix, suffix);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(contents);
            writer.close();
        }

        @Override
        public void close() {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            file = null;
        }

        private String getAbsolutePath() {
            return file.getAbsolutePath();
        }

    }

}
