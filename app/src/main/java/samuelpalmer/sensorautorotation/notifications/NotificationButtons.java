package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;

public class NotificationButtons {

    private final RemoteViews content;
    private final int buttonColourArgb;
    private final int unselectedBackgroundResource; // TODO: Remove this or use it
    private final Context context;
    
    /**
     * @param originalNotification Get this from the notification builder.
     * @param buttonColourArgb Get this from {@link NotificationStyles#getActionButtonColour()}
     */
    public NotificationButtons(Context context, int layoutResource, RemoteViews originalNotification, int buttonColourArgb, int unselectedBackgroundResource) {
        this.context = context;
        
        content = new RemoteViews(context.getPackageName(), layoutResource);
        content.removeAllViews(R.id.notification_content);
        content.addView(R.id.notification_content, originalNotification);

        this.buttonColourArgb = buttonColourArgb;
        this.unselectedBackgroundResource = unselectedBackgroundResource;
    }

    /**
     * @param icon 32 dip x 32 dip (including padding), white
     */
    public NotificationButtons configureButton(int id, int icon, int pendingIntentCode, Intent onClickIntent, CharSequence contentDescription, boolean selected) {
        content.setImageViewResource(id, icon);
        content.setInt(id, "setColorFilter", selected ? buttonColourArgb : ContextCompat.getColor(context, R.color.not_active));
        content.setOnClickPendingIntent(id, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));
        content.setInt(id, "setBackgroundResource", selected ? R.color.primary_500_background_active : android.R.color.transparent);
        
        if (VERSION.SDK_INT >= 15)
            content.setContentDescription(id, contentDescription);
        
        return this;
    }

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

}
