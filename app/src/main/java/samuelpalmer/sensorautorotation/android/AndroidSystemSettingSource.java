package samuelpalmer.sensorautorotation.android;

import android.content.ContentResolver;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettingSource;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.putString;

public class AndroidSystemSettingSource implements SystemSettingSource {

	private ContentResolver contentResolver;

	public AndroidSystemSettingSource(ContentResolver contentResolver) {
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
