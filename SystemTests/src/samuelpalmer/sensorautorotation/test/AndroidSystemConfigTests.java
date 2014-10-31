package samuelpalmer.sensorautorotation.test;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
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
		
		AndroidSystemSettingSource sut = new AndroidSystemSettingSource(contentResolver);
		
		String key = Settings.System.TIME_12_24;
		String originalValue = sut.getOrNull(key);
		
		try {
			sut.set(key, originalValue); //TODO: Somehow make the underlying putString method return false
		} catch (SettingWriteFailedException e) {
			assertEquals(key, e.getKey());
			assertEquals(originalValue, e.getValue());
			return;
		}
		
		fail("Not thrown");
	}
}
