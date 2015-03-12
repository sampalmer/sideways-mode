package samuelpalmer.sensorautorotation.settings.system;

import android.content.ContentResolver;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.putString;

public class AndroidSystemSettingSource implements SystemSettingSource {

	private final ContentResolver contentResolver;

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
