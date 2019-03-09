package samuelpalmer.memoryhog;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public abstract class MemoryHoggingService extends Service {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private MemoryHog memoryHog;

    @Override
    public void onCreate() {
        super.onCreate();

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(getClass().getSimpleName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(MainActivity.NOTIFICATION_CHANNEL_SERVICES);

        Notification notification = builder.getNotification();

        startForeground(getClass().getName().hashCode(), notification);

        memoryHog = new MemoryHog(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
