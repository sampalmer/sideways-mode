package samuelpalmer.sensorautorotation.settings.system;

public interface SystemSettingSource {
	String getOrNull(String key);

	void set(String key, String value) throws SettingWriteFailedException;
}
