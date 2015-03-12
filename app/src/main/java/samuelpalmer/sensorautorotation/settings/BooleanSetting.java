package samuelpalmer.sensorautorotation.settings;

import android.content.SharedPreferences;

class BooleanSetting extends SettingType<Boolean> {
	public void put(SharedPreferences.Editor editor, String key, Boolean value) {
		editor.putBoolean(key, value);
	}
}
