package samuelpalmer.sensorautorotation.systemconfiguration.tests;

import static org.junit.Assert.*;


import org.junit.Test;

import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverter;

public class SettingTests {

	@Test
	public void returnsNullForNonExistant() {
		InMemorySystemSettingSource config = new InMemorySystemSettingSource();
		String key = "123";
		Setting<String> sut = new Setting<String>(config, key, new VerifyingStringTypeConverter());
		
		String result = sut.getOrNull();
		
		assertNull(result);
	}
	
	class VerifyingStringTypeConverter implements TypeConverter<String> {

		public String tryParse(String nonNullTextValue) {
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
