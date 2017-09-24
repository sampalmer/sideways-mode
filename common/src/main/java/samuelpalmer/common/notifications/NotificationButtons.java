package samuelpalmer.common.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import samuelpalmer.common.R;

public class NotificationButtons {

	private final RemoteViews content;
	private final int buttonColourArgb;

	/**
	 * @param originalNotification Get this from the notification builder.
	 * @param buttonColourArgb Get this from {@link NotificationStyles#getActionButtonColour()}
	 */
	public NotificationButtons(Context context, int layoutResource, RemoteViews originalNotification, int buttonColourArgb) {
		content = new RemoteViews(context.getPackageName(), layoutResource);
		content.removeAllViews(R.id.notification_content);
		content.addView(samuelpalmer.common.R.id.notification_content, originalNotification);

		this.buttonColourArgb = buttonColourArgb;
	}

	/**
	 * @param icon 32 dip x 32 dip (including padding), white
	 */
	public NotificationButtons configureButton(int id, int icon, PendingIntent onClickIntent, CharSequence contentDescription) {
		content.setImageViewResource(id, icon);
		content.setInt(id, "setColorFilter", buttonColourArgb);
		content.setOnClickPendingIntent(id, onClickIntent);

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
