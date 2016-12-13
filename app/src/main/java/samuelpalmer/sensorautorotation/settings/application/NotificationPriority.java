package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationTimestamp;

@SuppressWarnings("unused")
public enum NotificationPriority {
	HIGH(NotificationBuilderCompat.PRIORITY_MAX, R.string.preference_notification_priority_high),
	NORMAL(NotificationBuilderCompat.PRIORITY_DEFAULT, R.string.preference_notification_priority_normal),
	LOW(NotificationBuilderCompat.PRIORITY_MIN, R.string.preference_notification_priority_low);

	private final int androidPriority;
	public final int stringResourceId;

	NotificationPriority(int androidPriority, int stringResourceId) {
		this.androidPriority = androidPriority;
		this.stringResourceId = stringResourceId;
	}

	public void apply(NotificationBuilderCompat builder) {
		builder.setPriority(androidPriority);
		if (this == NotificationPriority.HIGH)
			moveToTop(builder);
	}

	private void moveToTop(NotificationBuilderCompat builder) {
		builder.setWhen(NotificationTimestamp.highest());
	}

}
