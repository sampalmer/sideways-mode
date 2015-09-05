package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationParameters {

	public final Mode mode;
	public final boolean overridden;
	public final UserOrientation user;
	public final NotificationPriority priority;
	public final boolean onLockScreen;

	public NotificationParameters(Mode mode, boolean overridden, UserOrientation user, NotificationPriority priority, boolean onLockScreen) {
		this.mode = mode;
		this.overridden = overridden;
		this.user = user;
		this.priority = priority;
		this.onLockScreen = onLockScreen;
	}

	public CharSequence getSummary() {
		return StringResource.format(R.string.notification_summary, mode.autoRotationStateResourceId(), user.descriptionId());
	}

	public boolean equals(NotificationParameters other) {
		return
			mode.equals(other.mode)
			&&
			overridden == other.overridden
			&&
			user.equals(other.user)
			&&
			priority.equals(other.priority)
			&&
			onLockScreen == other.onLockScreen;
	}

}
