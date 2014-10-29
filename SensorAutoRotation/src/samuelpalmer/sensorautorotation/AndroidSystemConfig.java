package samuelpalmer.sensorautorotation;

import static android.provider.Settings.System.*;

import android.content.ContentResolver;

public class AndroidSystemConfig implements SystemConfig {
	
	private ContentResolver contentResolver;

	public AndroidSystemConfig(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
	
	@Override
	public Integer getOrNull(String key) {
		String textValue = getString(contentResolver, ACCELEROMETER_ROTATION);
		
		try {
            Integer value = textValue != null ? Integer.parseInt(textValue) : null;
			return value;
        } catch (NumberFormatException e) {
            return null;
        }
	}

	@Override
	public void set(String key, Integer value) throws SettingWriteFailedException {
		String textValue = value == null ? null : Integer.toString(value);
		boolean writeErrors = !putString(contentResolver, key, textValue);
		if (writeErrors)
			throw new SettingWriteFailedException(key, textValue);
	}

}
