package samuelpalmer.sensorautorotation.rules.lockscreen;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import java.io.IOException;

/**
 * Uses the new "locksettings" shell command to configure the secure lock screen
 */
@RequiresApi(26)
class ShellLockScreenRule extends LockScreenRule {

    public ShellLockScreenRule(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        super(activity);
    }

    @Override
    void enableLockScreenUnchecked() {
        runCommand("locksettings set-pin %s", PIN);
    }

    @Override
    void disableLockScreenUnchecked() {
        runCommand("locksettings clear --old %s", PIN);
    }

    private void runCommand(String format, Object... args) {
        String command = String.format(format, args);

        String response;
        try {
            response = device.executeShellCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response == null || response.equals(""))
            // Unfortunately, we don't get the exit code or error output, so the only way to detect an error is by checking standard output
            throw new RuntimeException("Unknown error when running shell command " + command);
    }

}
