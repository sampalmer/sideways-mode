package samuelpalmer.sensorautorotation.systemconfiguration;

public interface TypedSystemConfig<T> {
	T getOrNull(String key);
	void set(String key, T value) throws SettingWriteFailedException;
}
