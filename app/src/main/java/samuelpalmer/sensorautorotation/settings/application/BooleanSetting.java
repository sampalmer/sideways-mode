package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

public class BooleanSetting extends SettingType<Boolean> {
	public void put(SharedPreferences.Editor editor, String key, Boolean value) {
		editor.putBoolean(key, value);
	}
}
