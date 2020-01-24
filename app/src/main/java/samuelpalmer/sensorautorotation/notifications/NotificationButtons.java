package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;

public class NotificationButtons {

    private final RemoteViews content;

    @ColorInt
    private final int buttonColour;

    private final Context context;

    @ColorInt
    private final int inactiveIconColour;

    /**
     * @param originalNotification Get this from the notification builder.
     * @param buttonColour Get this from {@link NotificationStyles#getActionButtonColour()}
     */
    public NotificationButtons(Context context, int layoutResource, RemoteViews originalNotification, @ColorInt int textColour, @ColorInt int buttonColour) {
        this.context = context;

        content = new RemoteViews(context.getPackageName(), layoutResource);
        content.removeAllViews(R.id.notification_content);
        content.addView(R.id.notification_content, originalNotification);

        this.buttonColour = buttonColour;

        if (VERSION.SDK_INT <= 23)
            // On these Android versions, the notification text is already a grey colour, so we'll re-use that for consistency
            inactiveIconColour = textColour;
        else
            inactiveIconColour = ContextCompat.getColor(context, R.color.not_active);
    }

    /**
     * @param icon 32 dip x 32 dip (including padding), white
     */
    public NotificationButtons configureButton(int containerId, int buttonId, int icon, int pendingIntentCode, Intent onClickIntent, CharSequence contentDescription, boolean selected) {
        content.setInt(containerId, "setBackgroundResource", selected ? R.color.primary_500_background_active : android.R.color.transparent);
        content.setImageViewResource(buttonId, icon);
        content.setInt(buttonId, "setColorFilter", selected ? buttonColour : inactiveIconColour);
        content.setOnClickPendingIntent(buttonId, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));

        if (VERSION.SDK_INT >= 15)
            content.setContentDescription(buttonId, contentDescription);
        
        return this;
    }

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

}
