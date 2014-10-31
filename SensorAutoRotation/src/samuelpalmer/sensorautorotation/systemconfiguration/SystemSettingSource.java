package samuelpalmer.sensorautorotation.systemconfiguration;

public interface SystemSettingSource {
	String getOrNull(String key);
	void set(String key, String value) throws SettingWriteFailedException;
}
