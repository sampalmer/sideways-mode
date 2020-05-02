package samuelpalmer.sensorautorotation.notifications;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {

    public static NotificationCompat.Builder create(Context context, String channelId) {
        return new NotificationCompat.Builder(context, channelId)
            .setColor(getNotificationColour(context))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        ;
    }

    /**
     * Gets the colour to pass into {@link NotificationCompat.Builder#setColor(int)}.
     * This colour is automatically applied if you construct the notification through this class.
     */
    public static int getNotificationColour(Context context) {
        return ContextCompat.getColor(context, R.color.primary_500);
    }

}
