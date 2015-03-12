package samuelpalmer.sensorautorotation.settings;

import android.content.SharedPreferences;

class IntegerSetting extends SettingType<Integer> {
	public void put(SharedPreferences.Editor editor, String key, Integer value) {
		editor.putInt(key, value);
	}
}
