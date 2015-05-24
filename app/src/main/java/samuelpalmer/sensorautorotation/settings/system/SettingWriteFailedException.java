package samuelpalmer.sensorautorotation.settings.system;

public class SettingWriteFailedException extends Exception {

	private static final long serialVersionUID = 2703215740394090412L;

	public final String key;
	public final Object value;

	public SettingWriteFailedException(String key, Object value) {
		this.key = key;
		this.value = value;
	}

}
