package samuelpalmer.sensorautorotation.rules.lockscreen;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.io.IOException;

class LockScreenPinEnterer {

    private LockScreenPinEnterer(){}

    public static void enterPin(UiDevice device, String pin) {
        if (Build.VERSION.SDK_INT >= 21)
            enterPinWithShell(device, pin);
        else {
            // Unfortunately, shell commands cannot be run on API 18-19, so we need to click on the
            // number buttons instead
            enterPinWithUiAutomation(device, pin);
        }
    }

    @RequiresApi(21)
    private static void enterPinWithShell(UiDevice device, String pin) {
        try {
            device.executeShellCommand(String.format("input text %s", pin)); //Entering numbers
            device.executeShellCommand("input keyevent 66"); // Pressing enter
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enterPinWithUiAutomation(UiDevice device, String pin) {
        try {
            for (char numberCharacter : pin.toCharArray()) {
                UiObject numberButton = device.findObject(new UiSelector()
                        // API 18 uses package "android", whereas API 19 uses package "com.android.keyguard"
                        .packageNameMatches("android|com.android.keyguard")
                        .clickable(true)
                        .resourceIdMatches("(android|com.android.keyguard):id/key" + numberCharacter)
                );

                numberButton.click();
            }
            UiObject enterButton = device.findObject(new UiSelector()
                    .packageNameMatches("android|com.android.keyguard")
                    .clickable(true)
                    .resourceIdMatches("(android|com.android.keyguard):id/key_enter")
            );

            enterButton.click();
        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
