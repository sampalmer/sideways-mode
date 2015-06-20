package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;

@SuppressWarnings("unused")
public enum NotificationPriority {
	HIGH(NotificationBuilderCompat.PRIORITY_MAX),
	NORMAL(NotificationBuilderCompat.PRIORITY_DEFAULT),
	LOW(NotificationBuilderCompat.PRIORITY_MIN);

	private final int androidPriority;

	NotificationPriority(int androidPriority) {
		this.androidPriority = androidPriority;
	}

	public int androidPriority() {
		return androidPriority;
	}
}
