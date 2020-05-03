package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

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

    public NotificationButtons configureButton(
            @IdRes int containerId,
            @IdRes int iconUnselectedId,
            @IdRes int iconSelectedId,
            @IdRes int buttonId,
            @IdRes int progressId,
            int pendingIntentCode,
            Intent onClickIntent,
            boolean selected,
            boolean isLoading
    ) {
        content.setBoolean(containerId, "setEnabled", selected);

        // TODO: Having two separate views for the different icons causes them to randomly disappear on API 16
        content.setViewVisibility(iconUnselectedId, !isLoading && !selected ? View.VISIBLE : View.GONE);
        content.setViewVisibility(iconSelectedId, !isLoading && selected ? View.VISIBLE : View.GONE);
        content.setViewVisibility(progressId, isLoading ? View.VISIBLE : View.GONE);

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
