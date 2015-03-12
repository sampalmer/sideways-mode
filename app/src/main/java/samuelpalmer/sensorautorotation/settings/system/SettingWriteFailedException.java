package samuelpalmer.sensorautorotation.settings.system;

public class SettingWriteFailedException extends Exception {

	private static final long serialVersionUID = 2703215740394090412L;

	public SettingWriteFailedException(String key, Object value) {
		super(String.format("Failed to write the value '%s' for system setting '%s'.", value, key));
	}

}
