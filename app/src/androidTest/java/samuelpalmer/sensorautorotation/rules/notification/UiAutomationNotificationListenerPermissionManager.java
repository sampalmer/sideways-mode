package samuelpalmer.sensorautorotation.rules.notification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import samuelpalmer.sensorautorotation.utilities.ActivityUtils;

/**
 * Manages permissions by automating the Android settings user interface
 */
class UiAutomationNotificationListenerPermissionManager extends NotificationListenerPermissionManager {

    private final ActivityScenarioRule<? extends AppCompatActivity> activity;

    public UiAutomationNotificationListenerPermissionManager(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void grantAccess() {
        setPermission(true);
    }

    @Override
    public void revokeAccess() {
        setPermission(false);
    }

    private void setPermission(boolean targetState) {
        boolean hasPermission = isEnabled();
        if (hasPermission == targetState)
            return;

        Intent notificationListenersIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        activity.getScenario().onActivity(a -> a.startActivity(notificationListenersIntent));
        if (!device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 5000))
            throw new RuntimeException("Took too long for notification listener settings activity to start");

        try {
            UiObject2 listenerItem = getListenerItem();
            listenerItem.click();

            // We only get the confirmation dialog when we're enabling the listener
            if (!hasPermission) {
                UiObject2 okButton = getOkButton();
                okButton.click();
            }

            // We need to wait for the change to take effect before closing the activity. Otherwise it might not change!
            waitForListenerPermission(targetState);
        } finally {
            // Closing permission screen when we're done so the test activity is visible and can be closed at the end of the test
            activity.getScenario().onActivity(ActivityUtils::finishActivitiesOnTopOf);
        }
    }

    private UiObject2 getOkButton() {
        UiObject2 okButton = device.wait(Until.findObject(By
                        .pkg("com.android.settings")
                        .clickable(true)
                        .text("OK")
                ),
                1000
        );
        if (okButton == null)
            throw new RuntimeException("Couldn't find ok button to enable notification listener");

        return okButton;
    }

    private UiObject2 getListenerItem() {
        CharSequence listenerLabel = getListenerLabel();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        UiObject2 listenerItem = device.wait(Until.findObject(By
                    .pkg("com.android.settings")
                    .res("com.android.settings", "name")
                    .text(listenerLabel.toString())
                ),
                1000
        );
        if (listenerItem == null)
            throw new RuntimeException("Couldn't find notification listener in list");

        return listenerItem;
    }

    private CharSequence getListenerLabel() {
        PackageManager packageManager = targetContext.getPackageManager();

        ServiceInfo serviceInfo;
        try {
            serviceInfo = packageManager.getServiceInfo(listenerComponent, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        return serviceInfo.loadLabel(packageManager);
    }

}
