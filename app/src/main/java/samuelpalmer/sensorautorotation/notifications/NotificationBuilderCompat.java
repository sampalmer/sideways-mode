package samuelpalmer.sensorautorotation.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;

import samuelpalmer.sensorautorotation.Process;

/**
 * Basically a reinvented clone of NotificationCompat.Builder, except with a working setShowWhen
 * implementation. Avoiding use of the compat library reduces installation size and RAM use.
 */
@SuppressWarnings({"UnusedDeclaration", "UnusedReturnValue"})
public class NotificationBuilderCompat {
	protected final Notification.Builder builder;

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

	public static NotificationBuilderCompat create() {
		if (Build.VERSION.SDK_INT >= 21)
			return new V21();
		else if (Build.VERSION.SDK_INT >= 17)
			return new V17();
		else if (Build.VERSION.SDK_INT >= 16)
			return new V16();
		else
			return new NotificationBuilderCompat();
	}

	private NotificationBuilderCompat() {
		builder = new Notification.Builder(Process.context());
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
		return this;
	}

	public NotificationBuilderCompat setColor(int argb) {
		return this;
	}

	public NotificationBuilderCompat setVisibility(int visibility) {
		return this;
	}

	public NotificationBuilderCompat setWhen(long when) {
		builder.setWhen(when);
		return this;
	}

	public Notification build() {
		//noinspection deprecation
		return builder.getNotification();
	}

	public NotificationBuilderCompat setPriority(int pri) {
		return this;
	}

	public NotificationBuilderCompat setShowWhen(boolean show) {
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

	public NotificationBuilderCompat setContentInfo(CharSequence info) {
		builder.setContentInfo(info);
		return this;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static class V16 extends NotificationBuilderCompat
	{
		@Override
		public Notification build() {
			return builder.build();
		}

		@Override
		public NotificationBuilderCompat setPriority(int pri) {
			builder.setPriority(pri);
			return this;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private static class V17 extends V16
	{
		@Override
		public NotificationBuilderCompat setShowWhen(boolean show) {
			builder.setShowWhen(show);
			return this;
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class V21 extends V17
	{
		public V21() {
			setColor(0xFF795548);
		}

		@Override
		public NotificationBuilderCompat setCategory(String category) {
			builder.setCategory(category);
			return this;
		}

		@Override
		public NotificationBuilderCompat setColor(int argb) {
			builder.setColor(argb);
			return this;
		}

		@Override
		public NotificationBuilderCompat setVisibility(int visibility) {
			builder.setVisibility(visibility);
			return this;
		}
	}
}
