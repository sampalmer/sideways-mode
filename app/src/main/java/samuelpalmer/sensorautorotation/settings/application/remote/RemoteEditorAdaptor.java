package samuelpalmer.sensorautorotation.settings.application.remote;

import android.content.SharedPreferences;
import android.os.RemoteException;

import java.util.HashSet;
import java.util.Set;

class RemoteEditorAdaptor implements SharedPreferences.Editor {

	private final RemoteSharedPreferences remote;
	private final ParcelableEditor data = new ParcelableEditor();

	public RemoteEditorAdaptor(RemoteSharedPreferences remote) {
		this.remote = remote;
		data.mClear = false;
	}

	public SharedPreferences.Editor putString(String key, String value) {
		synchronized (this) {
			data.mModified.put(key, value);
			return this;
		}
	}

	public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
		synchronized (this) {
			data.mModified.put(key,
					(values == null) ? null : new HashSet<>(values));
			return this;
		}
	}

	public SharedPreferences.Editor putInt(String key, int value) {
		synchronized (this) {
			data.mModified.put(key, value);
			return this;
		}
	}

	public SharedPreferences.Editor putLong(String key, long value) {
		synchronized (this) {
			data.mModified.put(key, value);
			return this;
		}
	}

	public SharedPreferences.Editor putFloat(String key, float value) {
		synchronized (this) {
			data.mModified.put(key, value);
			return this;
		}
	}

	public SharedPreferences.Editor putBoolean(String key, boolean value) {
		synchronized (this) {
			data.mModified.put(key, value);
			return this;
		}
	}

	public SharedPreferences.Editor remove(String key) {
		synchronized (this) {
			data.mModified.put(key, null);
			return this;
		}
	}

	public SharedPreferences.Editor clear() {
		synchronized (this) {
			data.mClear = true;
			return this;
		}
	}

	@Override
	public boolean commit() {
		try {
			return remote.commit(data);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void apply() {
		try {
			remote.apply(data);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

}
