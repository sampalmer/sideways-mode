package samuelpalmer.sensorautorotation.systemconfiguration;

import static android.provider.Settings.System.*;

import android.content.ContentResolver;

public class AndroidSystemConfig implements SystemConfig {
	
	private ContentResolver contentResolver;

	public AndroidSystemConfig(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}
	
	@Override
	public String getOrNull(String key) {
		return getString(contentResolver, key);
	}

	@Override
	public void set(String key, String value) throws SettingWriteFailedException {
		boolean writeErrors = !putString(contentResolver, key, value);
		if (writeErrors)
			throw new SettingWriteFailedException(key, value);
	}

}
