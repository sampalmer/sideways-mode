package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.SemaphoreLocker.Operation;

public class MultiProcessSharedPreferences implements SharedPreferences {

	public final String preferencesName;
	private final SemaphoreLocker locker;

	public MultiProcessSharedPreferences(String name) {
		preferencesName = name;
		locker = new SemaphoreLocker(Process.context().getPackageName() + "." + preferencesName);		
	}

	@Override
	public Map<String, ?> getAll() {
		return locker.lock(new Operation<Map<String, ?>>() {

			@Override
			public Map<String, ?> run() {
				return preferences().getAll();
			}

		});
	}

	@Override
	public String getString(final String key, final String defValue) {
		return locker.lock(new Operation<String>() {

			@Override
			public String run() {
				return preferences().getString(key, defValue);
			}

		});
	}

	@Override
	public Set<String> getStringSet(final String key, final Set<String> defValues) {
		return locker.lock(new Operation<Set<String>>() {

			@Override
			public Set<String> run() {
				return preferences().getStringSet(key, defValues);
			}

		});
	}

	@Override
	public int getInt(final String key, final int defValue) {
		return locker.lock(new Operation<Integer>() {

			@Override
			public Integer run() {
				return preferences().getInt(key, defValue);
			}

		});
	}

	@Override
	public long getLong(final String key, final long defValue) {
		return locker.lock(new Operation<Long>() {

			@Override
			public Long run() {
				return preferences().getLong(key, defValue);
			}

		});
	}

	@Override
	public float getFloat(final String key, final float defValue) {
		return locker.lock(new Operation<Float>() {

			@Override
			public Float run() {
				return preferences().getFloat(key, defValue);
			}

		});
	}

	@Override
	public boolean getBoolean(final String key, final boolean defValue) {
		return locker.lock(new Operation<Boolean>() {

			@Override
			public Boolean run() {
				return preferences().getBoolean(key, defValue);
			}

		});
	}

	@Override
	public boolean contains(final String key) {
		return locker.lock(new Operation<Boolean>() {

			@Override
			public Boolean run() {
				return preferences().contains(key);
			}

		});
	}

	@Override
	public Editor edit() {
		return new MultiProcessEditor(preferences(), preferencesName, locker);
	}

	@Override
	public synchronized void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		if (!listeners.containsKey(listener)) {
			MultiProcessChangeReceiver receiver = new MultiProcessChangeReceiver(this, listener);
			receiver.register();
			listeners.put(listener, receiver);
		}
	}

	@Override
	public synchronized void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		MultiProcessChangeReceiver receiver = listeners.remove(listener);
		if (receiver != null)
			receiver.unregister();
	}

	private final Map<OnSharedPreferenceChangeListener, MultiProcessChangeReceiver> listeners = new HashMap<>();

	/**
	 * Makes a new instance on each call since apparently that circumvents process-wide caching.
	 *
	 * See https://code.google.com/p/android/issues/detail?id=66625
	 */
	private SharedPreferences preferences() {
		return Process.context().getSharedPreferences(
			preferencesName,
			Context.MODE_MULTI_PROCESS //This actually just causes the preferences to be reloaded from storage in this method call
		);
	}

}
