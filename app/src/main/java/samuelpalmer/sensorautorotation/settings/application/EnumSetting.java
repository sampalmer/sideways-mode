package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

public class EnumSetting<T extends Enum<T>> extends SettingType<T> {
	private final Class<T> clazz;

	public EnumSetting(Class<T> clazz) {
		this.clazz = clazz;
	}

	public void put(SharedPreferences.Editor editor, String key, T value) {
		editor.putString(key, value.name());
	}

	@Override
	public T convertFromRaw(Object raw) {
		return Enum.valueOf(clazz, (String)raw);
	}
}
