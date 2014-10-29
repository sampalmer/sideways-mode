package samuelpalmer.sensorautorotation;

public interface SystemConfig {
	Integer getOrNull(String key);
	void set(String key, Integer value) throws SettingWriteFailedException;
}
