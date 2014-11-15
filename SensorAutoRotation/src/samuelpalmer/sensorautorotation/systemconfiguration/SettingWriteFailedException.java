package samuelpalmer.sensorautorotation.systemconfiguration;

public class SettingWriteFailedException extends Exception {

	private static final long serialVersionUID = 2703215740394090412L;
	private String key;
	private Object value;
	
	public SettingWriteFailedException(String key, Object value) {
		super(String.format("Failed to write the value '%s' for setting '%s'", value, key));
		
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public Object getValue() {
		return value;
	}
	
}
