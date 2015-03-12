package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

abstract class SettingType<T> {
	@SuppressWarnings("unchecked")
	public T convertFromRaw(Object raw) {
		return (T)raw;
	}

	public abstract void put(SharedPreferences.Editor editor, String key, T value);
}
