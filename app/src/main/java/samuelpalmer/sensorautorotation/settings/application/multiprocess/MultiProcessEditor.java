package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.SemaphoreLocker.Operation;

class MultiProcessEditor implements Editor {

	private final UnderlyingSharedPreferencesFactory factory;
	private final String prefsName;
	private final SemaphoreLocker locker;

	@SuppressLint("CommitPrefEdits")
	public MultiProcessEditor(UnderlyingSharedPreferencesFactory underlyingPrefs, String prefsName, SemaphoreLocker locker) {
		this.factory = underlyingPrefs;
		this.prefsName = prefsName;
		this.locker = locker;
	}

	private final Map<String, Object> mModified = new HashMap<>();
	private boolean mClear = false;

	public Editor putString(String key, String value) {
		synchronized (this) {
			mModified.put(key, value);
			return this;
		}
	}
	public Editor putStringSet(String key, Set<String> values) {
		synchronized (this) {
			mModified.put(key,
					(values == null) ? null : new HashSet<>(values));
			return this;
		}
	}
	public Editor putInt(String key, int value) {
		synchronized (this) {
			mModified.put(key, value);
			return this;
		}
	}
	public Editor putLong(String key, long value) {
		synchronized (this) {
			mModified.put(key, value);
			return this;
		}
	}
	public Editor putFloat(String key, float value) {
		synchronized (this) {
			mModified.put(key, value);
			return this;
		}
	}
	public Editor putBoolean(String key, boolean value) {
		synchronized (this) {
			mModified.put(key, value);
			return this;
		}
	}

	public Editor remove(String key) {
		synchronized (this) {
			mModified.put(key, this);
			return this;
		}
	}

	public Editor clear() {
		synchronized (this) {
			mClear = true;
			return this;
		}
	}

	public boolean commit() {
		synchronized (this) {
			return locker.lock(new Operation<Boolean>() {

				@Override
				public Boolean run() {

					SharedPreferences underlyingPrefs = factory.preferences();
					Editor editor = underlyingPrefs.edit();

					if (mClear)
						editor.clear();

					for (Map.Entry<String, Object> e : mModified.entrySet()) {
						String key = e.getKey();
						Object value = e.getValue();

						// "this" is the magic value for a removal mutation. In addition,
						// setting a value to "null" for a given key is specified to be
						// equivalent to calling remove on that key.
						if (value == MultiProcessEditor.this || value == null)
							editor.remove(key);
						else {
							if (value instanceof String)
								editor.putString(key, (String) value);
							else if (value instanceof Set)
								//noinspection unchecked
								editor.putStringSet(key, (Set<String>) value);
							else if (value instanceof Integer)
								editor.putInt(key, (int) value);
							else if (value instanceof Long)
								editor.putLong(key, (long) value);
							else if (value instanceof Float)
								editor.putFloat(key, (float) value);
							else if (value instanceof Boolean)
								editor.putBoolean(key, (boolean) value);
							else
								throw new RuntimeException("Unsupported value type: " + value.getClass());
						}
					}

					boolean result = editor.commit();

					//Note that this is different to the Android implementation in that this broadcasts all _requested_ changes rather than all _actual_ changes.
					MultiProcessChangeReceiver.broadcastChange(prefsName, new ArrayList<>(mModified.keySet()));

					mClear = false;
					mModified.clear();

					return result;
				}

			});
		}
	}

	public void apply() {
		// This _must_ be done synchronously because we don't want the file to change outside of the
		// semaphore.
		commit();
	}

}
