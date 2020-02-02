package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public abstract class EnvironmentErrorManager {
    
    public static void report(Context context, EnvironmentError error) {
        // This is useful when checking log output from Pre-launch report on Google Play Store.
        Log.w(EnvironmentError.class.getSimpleName(), error.getSummary() + ". " + error.getClass().getSimpleName() + ": " + error.logMessage());
        
        postNotification(context, error);
        
        ServiceSettings
            .getSharedPreferences(context)
            .edit()
            .putString(ServiceSettings.KEY_LAST_ERROR_JSON, error.toJson())
            .apply();
        
        new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(context);
    }
    
    public static void clear(Context context) {
        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        assert notificationManager != null;
        notificationManager.cancel(NotificationIds.ERROR);
        
        ServiceSettings
            .getSharedPreferences(context)
            .edit()
            .remove(ServiceSettings.KEY_LAST_ERROR_JSON)
            .apply();
    }
    
    private static void postNotification(Context context, EnvironmentError error) {
        PendingIntent clearIntentIntent = PendingIntent.getBroadcast(
            context,
            PendingIntentIds.ENVIRONMENT_ERROR_NOTIFICATION_CLEAR,
            new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).asIntent(context),
            PendingIntentIds.PENDING_INTENT_FLAGS
        );
        
        NotificationBuilderCompat builder = NotificationBuilderFactory.create(context)
            .setSmallIcon(R.drawable.notification_error) // TODO: Maybe just use the app's normal icon so it's clear which app posted this notification
            .setContentIntent(error.showError(context))
            .setContentTitle(error.getSummary())
            .setContentText(error.getMessage(context))
            .setDeleteIntent(clearIntentIntent)
            .setTicker(error.getSummary()) // Helps ensure the user actually notices that the error occurred. Only works on Android 4.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setChannelId(NotificationChannels.ID_ERRORS);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
            // Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
            // the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
            builder.setVibrate(new long[]{});
        
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NotificationIds.ERROR, notification);
    }
    
}
