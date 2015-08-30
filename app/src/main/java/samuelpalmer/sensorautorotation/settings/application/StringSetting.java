package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences.Editor;

public class StringSetting extends SettingType<String> {
	@Override
	public void put(Editor editor, String key, String value) {
		editor.putString(key, value);
	}
}
