package samuelpalmer.sensorautorotation.systemconfiguration.tests;

import java.util.HashMap;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettingSource;

public class InMemorySystemSettingSource implements SystemSettingSource {
	
	private final HashMap<String, String> values = new HashMap<String, String>();
	
	public String getOrNull(String key) {
		return values.getOrDefault(key, null);
	}

	public void set(String key, String value)
			throws SettingWriteFailedException {
		values.put(key, value);
	}
}