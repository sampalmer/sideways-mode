package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import samuelpalmer.sensorautorotation.PendingIntentIds;

/**
 * Assembles a custom notification layout that includes inline buttons in the main notification
 * content. This is more convenient for the user than stock notification actions because the
 * buttons are always visible and don't increase the height of the notification.
 */
public class NotificationButtons {

    private final RemoteViews content;

    private final Context context;

    public NotificationButtons(Context context, int layoutResource) {
        this.context = context;

        content = new RemoteViews(context.getPackageName(), layoutResource);
    }

    /**
     * @param icon 32 dip x 32 dip (including padding), white
     */
    public NotificationButtons configureButton(
            @IdRes int containerId,
            @IdRes int buttonId,
            @IdRes int progressOnColourDarkThemeId,
            @IdRes int progressOnColourLightThemeId,
            @DrawableRes int icon,
            int pendingIntentCode,
            Intent onClickIntent,
            boolean selected,
            boolean isLoading
    ) {
        content.setBoolean(containerId, "setEnabled", selected);

        // Not hiding the button altogether when loading since then the user can click the notification body behind it
        content.setImageViewResource(buttonId, isLoading ? 0 : icon);

        // Unfortunately, the progress bar colour cannot be changed directly, so we'll switch between pre-defined progress bars to achieve the same effect
        content.setViewVisibility(progressOnColourDarkThemeId, View.GONE);
        content.setViewVisibility(progressOnColourLightThemeId, View.GONE);

        // TODO: Show the appropriate progress bar. For this, either usage setImageLevel() or

        content.setOnClickPendingIntent(buttonId, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));

        return this;
    }

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

}
