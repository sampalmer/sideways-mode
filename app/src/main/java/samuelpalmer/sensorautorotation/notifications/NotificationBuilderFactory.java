package samuelpalmer.sensorautorotation.notifications;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.R;

/**
 * Contains static helper methods for building notifications
 */
public class NotificationBuilderFactory {

    private NotificationBuilderFactory() {}

    /**
     * Initialises a {@link NotificationCompat.Builder} with the the app's notification colour
     * and {@link NotificationCompat#VISIBILITY_PUBLIC}.
     */
    public static NotificationCompat.Builder create(Context context, String channelId) {
        int color = ContextCompat.getColor(context, R.color.primary_500);

        return new NotificationCompat.Builder(context, channelId)
            .setColor(color)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        ;
    }

}
