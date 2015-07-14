package samuelpalmer.sensorautorotation.notifications.service;

public class NotificationParameters {
	public final Mode mode;
	public final boolean overridden;
	public final CharSequence summary;

	public NotificationParameters(Mode mode, boolean overridden, CharSequence summary) {
		this.mode = mode;
		this.overridden = overridden;
		this.summary = summary;
	}
}
