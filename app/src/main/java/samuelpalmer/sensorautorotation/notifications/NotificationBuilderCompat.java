package samuelpalmer.sensorautorotation.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

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

	public final Property<Integer> smallIcon = new Property<Integer>() {
		@Override
		protected void setInternal(Integer value) {
			builder.setSmallIcon(value);
		}
	};

	public final Property<PendingIntent> contentIntent = new Property<PendingIntent>() {
		@Override
		protected void setInternal(PendingIntent value) {
			builder.setContentIntent(value);
		}
	};

	public final Property<CharSequence> contentTitle = new Property<CharSequence>() {
		@Override
		protected void setInternal(CharSequence value) {
			builder.setContentTitle(value);
		}
	};

	public final Property<CharSequence> contentText = new Property<CharSequence>() {
		@Override
		protected void setInternal(CharSequence value) {
			builder.setContentText(value);
		}
	};

	public final Property<Long> when = new Property<Long>() {
		@Override
		protected void setInternal(Long value) {
			builder.setWhen(value);
		}
	};

	public final Property<Boolean> autoCancel = new Property<Boolean>() {
		@Override
		protected void setInternal(Boolean value) {
			builder.setAutoCancel(value);
		}
	};

	public final Property<CharSequence> ticker = new Property<CharSequence>() {
		@Override
		protected void setInternal(CharSequence value) {
			builder.setTicker(value);
		}
	};

	public static NotificationBuilderCompat create(Context context) {
		if (Build.VERSION.SDK_INT >= 21)
			return new V21(context);
		else if (Build.VERSION.SDK_INT >= 17)
			return new V17(context);
		else if (Build.VERSION.SDK_INT >= 16)
			return new V16(context);
		else
			return new NotificationBuilderCompat(context);
	}

	private NotificationBuilderCompat(Context context) {
		builder = new Notification.Builder(context);
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

	public abstract class Property<T> {
		public NotificationBuilderCompat set(T value) {
			setInternal(value);
			return NotificationBuilderCompat.this;
		}

		protected abstract void setInternal(T value);
	}

	private class NullProperty<T> extends Property<T> {
		@Override
		protected void setInternal(T value) {}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static class V16 extends NotificationBuilderCompat
	{
		public V16(Context context) {
			super(context);
		}

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
		public V17(Context context) {
			super(context);
		}

		@Override
		public NotificationBuilderCompat setShowWhen(boolean show) {
			builder.setShowWhen(show);
			return this;
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class V21 extends V17
	{
		public V21(Context context) {
			super(context);
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
