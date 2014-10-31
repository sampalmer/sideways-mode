package samuelpalmer.sensorautorotation.systemconfiguration;

public interface SystemConfig {
	String getOrNull(String key);
	void set(String key, String value) throws SettingWriteFailedException;
}
