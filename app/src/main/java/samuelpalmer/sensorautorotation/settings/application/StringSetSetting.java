package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import java.util.Set;

public class StringSetSetting extends SettingType<Set<String>> {

	@Override
	public void put(SharedPreferences.Editor editor, String key, Set<String> value) {
		editor.putStringSet(key, value);
	}

}
