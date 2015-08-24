package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

public class IntegerSetting extends SettingType<Integer> {
	public void put(SharedPreferences.Editor editor, String key, Integer value) {
		editor.putInt(key, value);
	}
}
