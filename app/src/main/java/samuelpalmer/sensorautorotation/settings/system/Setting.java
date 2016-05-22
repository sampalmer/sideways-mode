package samuelpalmer.sensorautorotation.settings.system;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Build.VERSION;
import android.os.Handler;
import android.provider.Settings;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.system.types.TypeConverter;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.getUriFor;
import static android.provider.Settings.System.putString;

public class Setting<T> {

	private final ContentResolver contentResolver;
	private final String key;
	private final TypeConverter<T> converter;
	private final T defaultValue;

	public Setting(String key, TypeConverter<T> converter, T defaultValue) {
		this.contentResolver = Process.context().getContentResolver();
		this.key = key;
		this.converter = converter;
		this.defaultValue = defaultValue;
	}

	private final ChangeRegistrar onChange = new ChangeRegistrar();
	public final EventRegistrar onChange() { return onChange; }

	public T get() {
		String value = getOrNull(key);
		if (value == null)
			return defaultValue;

		return converter.tryParse(value);
	}

	public void set(T value) {
		String textValue = converter.format(value);
		set(key, textValue);
	}

	private String getOrNull(String key) {
		return getString(contentResolver, key);
	}

	private void set(String key, String value) {
		if (
			VERSION.SDK_INT >= 23
			&&
			!Settings.System.canWrite(Process.context())
		) {
			EnvironmentError.report(R.string.environment_error_permission_needed_summary, R.string.environment_error_system_setting_write_denied_message);
			return;
		}

		boolean writeErrors = !putString(contentResolver, key, value);
		if (writeErrors) {
			//Because the developer can't do anything about this; it seems to be a problem on the device.
			EnvironmentError.report(R.string.environment_error_system_setting_write_failure_summary, R.string.environment_error_system_setting_write_failure_message, key, value);
		}
	}

	//Mostly copied from ApplicationSettings.Setting
	private class ChangeRegistrar extends EventRegistrarAdaptor {

		private final ContentObserver listener = new ContentObserver(new Handler() /* Otherwise the callback is called in a different thread */ ) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				report();
			}
		};

		@Override
		protected void subscribeToAdaptee() {
			contentResolver.registerContentObserver(getUriFor(key), false, listener);
		}

		@Override
		protected void unsubscribeFromAdaptee() {
			contentResolver.unregisterContentObserver(listener);
		}

	}
}
