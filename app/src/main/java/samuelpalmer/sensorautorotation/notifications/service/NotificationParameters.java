package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class NotificationParameters {

	public final Mode mode;
	public final boolean overridden;
	public final UserOrientation user;

	public NotificationParameters(Mode mode, boolean overridden, UserOrientation user) {
		this.mode = mode;
		this.overridden = overridden;
		this.user = user;
	}

	public CharSequence getSummary() {
		return StringResource.format(R.string.notification_summary, mode.autoRotationStateResourceId(), user.descriptionId());
	}

}
