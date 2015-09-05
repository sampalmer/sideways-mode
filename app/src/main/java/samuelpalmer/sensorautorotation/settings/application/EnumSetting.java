package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

public class EnumSetting<T extends Enum<T>> extends SettingType<T> {
	private final Class<T> clazz;

	public EnumSetting(Class<T> clazz) {
		this.clazz = clazz;
	}

	public void put(SharedPreferences.Editor editor, String key, T value) {
		editor.putString(key, convertToRaw(value));
	}

	@Override
	public boolean supportsNull() {
		return true;
	}

	@Override
	public T getOrDefault(SharedPreferences prefs, String key, T defaultValue) {
		String raw = prefs.getString(key, convertToRaw(defaultValue));
		if (raw == null)
			return null;

		return convertFromRaw(raw);
	}

	private String convertToRaw(T value) {
		return value.name();
	}

	@Override
	public T convertFromRaw(Object raw) {
		return Enum.valueOf(clazz, (String)raw);
	}
}
