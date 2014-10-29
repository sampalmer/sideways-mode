package samuelpalmer.sensorautorotation.test;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidUntypedSystemConfig;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import android.content.ContentResolver;
import android.provider.Settings;
import android.test.AndroidTestCase;

public class AndroidSystemConfigTests extends AndroidTestCase {

	public void testGetReturnsNullForUnknownKey() {
		fail("Not implemented");
	}
	
	public void testGetReturnsNullForNonInteger() {
		fail("Not implemented");
	}
	
	public void testRoundTripsNonNullValue() {
		fail("Not implemented");
	}
	
	public void testRoundTripsNullValue() {
		fail("Not implemented");
	}
	
	public void testThrowsOnWriteFailure() {
		ContentResolver contentResolver = getContext().getContentResolver();
		
		String key = Settings.System.TIME_12_24;
		String originalValue = Settings.System.getString(contentResolver, key);
		
		AndroidUntypedSystemConfig sut = new AndroidUntypedSystemConfig(contentResolver);
		int value = 0;
		
		try {
			sut.set(key, value); //TODO: Somehow make the underlying putString method return false
		} catch (SettingWriteFailedException e) {
			assertEquals(key, e.getKey());
			assertEquals(value, e.getValue());
			return;
		}
		
		Settings.System.putString(contentResolver, key, originalValue);
		fail("Not thrown");
	}
}
