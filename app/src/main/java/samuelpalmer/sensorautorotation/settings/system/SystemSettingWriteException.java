package samuelpalmer.sensorautorotation.settings.system;

/**
 * A write error occurred when writing a system setting.
 */
public class SystemSettingWriteException extends Exception {
	
	public final String key;
	public final String value;
	
	public SystemSettingWriteException(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
}
