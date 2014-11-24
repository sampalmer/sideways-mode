package samuelpalmer.sensorautorotation.systemconfiguration;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.putString;
import android.content.ContentResolver;

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
