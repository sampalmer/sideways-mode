package samuelpalmer.common.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;

public class NotificationButtons {

	private final RemoteViews content;
	private final int buttonColourArgb;
	private final int unselectedBackgroundResource;
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
		content.setInt(id, "setColorFilter", buttonColourArgb);
		content.setOnClickPendingIntent(id, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));
		content.setInt(id, "setBackgroundResource", selected ? R.drawable.button_selectable_selected : unselectedBackgroundResource);
		
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
