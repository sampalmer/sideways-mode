package samuelpalmer.common.settings.application;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

public class StringSetSetting extends SettingType<Set<String>> {

	@Override
	public void put(SharedPreferences.Editor editor, String key, Set<String> value) {
		editor.putStringSet(key, value);
	}

	@Override
	public boolean supportsNull() {
		return true;
	}

	@Override
	public Set<String> getOrDefault(SharedPreferences prefs, String key, Set<String> defaultValue) {
		Set<String> result = prefs.getStringSet(key, defaultValue);
		
		// The documentation for getStringSet says you're not allowed to modify and re-save its result.
		// If you do so, it silently succeeds, but the changes never get written to disk, and listeners aren't notified.
		// See https://stackoverflow.com/q/9803838/238753.
		return Collections.unmodifiableSet(result);
	}

}
