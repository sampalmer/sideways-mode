package samuelpalmer.sensorautorotation.settings.application.remote.processprefs;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;
import java.util.Set;

import samuelpalmer.sensorautorotation.settings.application.remote.processprefs.SemaphoreLocker.Operation;

class MultiProcessEditor implements Editor {

	private final SharedPreferences underlyingPrefs;
	private final String prefsName;
	private final Editor editor;
	private final SemaphoreLocker locker;

	@SuppressLint("CommitPrefEdits")
	public MultiProcessEditor(SharedPreferences underlyingPrefs, String prefsName, SemaphoreLocker locker) {
		this.underlyingPrefs = underlyingPrefs;
		this.prefsName = prefsName;
		this.locker = locker;

		this.editor = locker.lock(new Operation<Editor>() {
			@Override
			public Editor run() {
				return MultiProcessEditor.this.underlyingPrefs.edit();
			}
		});
	}

	public Editor putString(String key, String value) {
		return editor.putString(key, value);
	}

	public Editor putStringSet(String key, Set<String> values) {
		return editor.putStringSet(key, values);
	}

	public Editor putInt(String key, int value) {
		return editor.putInt(key, value);
	}

	public Editor putLong(String key, long value) {
		return editor.putLong(key, value);
	}

	public Editor putFloat(String key, float value) {
		return editor.putFloat(key, value);
	}

	public Editor putBoolean(String key, boolean value) {
		return editor.putBoolean(key, value);
	}

	public Editor remove(String key) {
		return editor.remove(key);
	}

	public Editor clear() {
		return editor.clear();
	}

	public boolean commit() {
		return locker.lock(new Operation<Boolean>() {

			@Override
			public Boolean run() {
				final ArrayList<String> affectedKeys = new ArrayList<>();

				OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
						affectedKeys.add(key);
					}
				};

				underlyingPrefs.registerOnSharedPreferenceChangeListener(listener);
				boolean result = editor.commit();
				underlyingPrefs.unregisterOnSharedPreferenceChangeListener(listener);

				MultiProcessChangeReceiver.broadcastChange(prefsName, affectedKeys);

				return result;
			}

		});
	}

	public void apply() {
		// This _must_ be done synchronously because we don't want the file to change outside of the
		// semaphore.
		commit();
	}

}
