package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

/**
 * Basically a reinvented clone of NotificationCompat.Builder, except with a working setShowWhen
 * implementation. Avoiding use of the compat library reduces installation size and RAM use.
 */
@SuppressWarnings("UnusedDeclaration")
public class NotificationBuilderCompat {
	private final Notification.Builder builder;

	public static final int PRIORITY_DEFAULT = 0;
	public static final int PRIORITY_LOW = -1;
	public static final int PRIORITY_MIN = -2;
	public static final int PRIORITY_HIGH = 1;
	public static final int PRIORITY_MAX = 2;

	public static final int VISIBILITY_PUBLIC = 1;
	public static final int VISIBILITY_PRIVATE = 0;
	public static final int VISIBILITY_SECRET = -1;

	public static final String CATEGORY_CALL = "call";
	public static final String CATEGORY_MESSAGE = "msg";
	public static final String CATEGORY_EMAIL = "email";
	public static final String CATEGORY_EVENT = "event";
	public static final String CATEGORY_PROMO = "promo";
	public static final String CATEGORY_ALARM = "alarm";
	public static final String CATEGORY_PROGRESS = "progress";
	public static final String CATEGORY_SOCIAL = "social";
	public static final String CATEGORY_ERROR = "err";
	public static final String CATEGORY_TRANSPORT = "transport";
	public static final String CATEGORY_SYSTEM = "sys";
	public static final String CATEGORY_SERVICE = "service";
	public static final String CATEGORY_RECOMMENDATION = "recommendation";
	public static final String CATEGORY_STATUS = "status";

	public NotificationBuilderCompat(Context context) {
		builder = new Notification.Builder(context);
	}

	public NotificationBuilderCompat setSmallIcon(int icon) {
		builder.setSmallIcon(icon);
		return this;
	}

	public NotificationBuilderCompat setContentIntent(PendingIntent intent) {
		builder.setContentIntent(intent);
		return this;
	}

	public NotificationBuilderCompat setContentTitle(CharSequence title) {
		builder.setContentTitle(title);
		return this;
	}

	public NotificationBuilderCompat setContentText(CharSequence text) {
		builder.setContentText(text);
		return this;
	}

	public NotificationBuilderCompat setCategory(String category) {
		if (Build.VERSION.SDK_INT >= 21)
			builder.setCategory(category);
		return this;
	}

	public NotificationBuilderCompat setColor(int argb) {
		if (Build.VERSION.SDK_INT >= 21)
			builder.setColor(argb);
		return this;
	}

	public NotificationBuilderCompat setVisibility(int visibility) {
		if (Build.VERSION.SDK_INT >= 21)
			builder.setVisibility(visibility);
		return this;
	}

	public NotificationBuilderCompat setWhen(long when) {
		builder.setWhen(when);
		return this;
	}

	public Notification build() {
		if (Build.VERSION.SDK_INT >= 16)
			return builder.build();
		else
			//noinspection deprecation
			return builder.getNotification();
	}

	public NotificationBuilderCompat setPriority(int pri) {
		if (Build.VERSION.SDK_INT >= 16)
			builder.setPriority(pri);
		return this;
	}

	public NotificationBuilderCompat setShowWhen(boolean show) {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(show);
		return this;
	}

	public NotificationBuilderCompat setAutoCancel(boolean autoCancel) {
		builder.setAutoCancel(autoCancel);
		return this;
	}

	public NotificationBuilderCompat setTicker(CharSequence tickerText) {
		builder.setTicker(tickerText);
		return this;
	}
}
