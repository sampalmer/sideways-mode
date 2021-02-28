package samuelpalmer.sensorautorotation.components;

import android.service.notification.NotificationListenerService;

public class TestingNotificationListener extends NotificationListenerService {

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
    }

}
