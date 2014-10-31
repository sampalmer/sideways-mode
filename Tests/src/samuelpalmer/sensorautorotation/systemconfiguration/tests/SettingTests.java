package samuelpalmer.sensorautorotation.systemconfiguration.tests;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemConfig;
import samuelpalmer.sensorautorotation.systemconfiguration.types.SystemConfigTypeConverter;

public class SettingTests {

	@Test
	public void returnsNullForNonExistant() {
		InMemorySystemConfig config = new InMemorySystemConfig();
		String key = "123";
		Setting<String> sut = new Setting<String>(config, key, new VerifyingStringTypeConverter());
		
		String result = sut.getOrNull();
		
		assertNull(result);
	}
	
	class InMemorySystemConfig implements SystemConfig {
		
		private final HashMap<String, String> values = new HashMap<String, String>();
		
		public String getOrNull(String key) {
			return values.getOrDefault(key, null);
		}

		public void set(String key, String value)
				throws SettingWriteFailedException {
			values.put(key, value);
		}
	}
	
	class VerifyingStringTypeConverter implements SystemConfigTypeConverter<String> {

		public String parse(String nonNullTextValue) {
			verify(nonNullTextValue);
			return nonNullTextValue;
		}

		public String format(String nonNullValue) {
			verify(nonNullValue);
			return nonNullValue;
		}

		private void verify(String nonNullValue) {
			if (nonNullValue == null)
				throw new RuntimeException("Got null value");
		}
		
	}
}
