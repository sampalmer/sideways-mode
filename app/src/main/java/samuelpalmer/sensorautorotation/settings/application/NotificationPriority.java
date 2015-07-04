package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationTimestamp;

@SuppressWarnings("unused")
public enum NotificationPriority {
	HIGH(NotificationBuilderCompat.PRIORITY_MAX),
	NORMAL(NotificationBuilderCompat.PRIORITY_DEFAULT),
	LOW(NotificationBuilderCompat.PRIORITY_MIN);

	private final int androidPriority;

	NotificationPriority(int androidPriority) {
		this.androidPriority = androidPriority;
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
