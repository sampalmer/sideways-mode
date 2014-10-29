package samuelpalmer.sensorautorotation.tests;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;

public class SettingWriteFailedExceptionTests {

	@Test
	public void formatsInput() {
		String key = "key";
		String value = "value";
		
		SettingWriteFailedException sut = new SettingWriteFailedException(key, value);
		String message = sut.toString();
		
		assertThat(message, containsString(key));
		assertThat(message, containsString(value));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void mentionsRemovalForNullValue() {
		String message = new SettingWriteFailedException("key", null).toString();
		
		assertThat(
			message,
			anyOf(
				containsString("null"),
				containsString("remov"),
				containsString("clear"),
				containsString("delet")
			)
		);
	}
	
}
