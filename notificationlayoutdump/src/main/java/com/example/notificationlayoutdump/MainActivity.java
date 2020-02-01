package com.example.notificationlayoutdump;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String NOTIFICATION_CHANNEL_ID = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager notificationManager = ContextCompat.getSystemService(this, NotificationManager.class);
        assert notificationManager != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        dumpNotificationLayout(this);
    }

    private static void dumpNotificationLayout(Context context) {
        RemoteViews notificationContent = makeNotificationRemoteViews(context);

        FrameLayout host = new FrameLayout(context);
        View inflated = notificationContent.apply(context, host);

        String stringifiedNotification = ViewLayoutDumper.viewToXml(inflated);
        ClipboardManager clipboardManager = ContextCompat.getSystemService(context, ClipboardManager.class);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Notification layout", stringifiedNotification));
        Toast.makeText(context, "Notification copied to clipboard", Toast.LENGTH_LONG).show();
    }

    private static RemoteViews makeNotificationRemoteViews(Context context) {
        RemoteViews customContentView = new RemoteViews(context.getPackageName(), R.layout.notification_contents);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, NOTIFICATION_CHANNEL_ID) : new Notification.Builder(context);

        if (Build.VERSION.SDK_INT >= 21)
            builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));


        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);

        if (Build.VERSION.SDK_INT >= 24)
            builder
                    .setCustomContentView(customContentView)
                    .setStyle(new Notification.DecoratedCustomViewStyle())
            ;
        else
            builder.setContentTitle(context.getText(R.string.app_name));

        RemoteViews notificationContent;
        if (Build.VERSION.SDK_INT >= 24)
            notificationContent = builder.createContentView();
        else if (Build.VERSION.SDK_INT >= 16)
            notificationContent = builder.build().contentView;
        else
            notificationContent = builder.getNotification().contentView;

        return notificationContent;
    }
}
