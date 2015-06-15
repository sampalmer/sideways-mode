package samuelpalmer.sensorautorotation.settings.application.remote;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RemoteSharedPreferencesServer extends RemoteSharedPreferences.Stub {

	private final SharedPreferences prefs;

	public RemoteSharedPreferencesServer(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	@Override
	public synchronized Map getAll() throws RemoteException {
		return prefs.getAll();
	}

	@Override
	public synchronized String getString(String key, String defValue) throws RemoteException {
		return prefs.getString(key, defValue);
	}

	@Override
	public synchronized String[] getStringSet(String key, String[] defValues) throws RemoteException {
		Set<String> result = prefs.getStringSet(key, new HashSet<>(Arrays.asList(defValues)));
		return result.toArray(new String[result.size()]);
	}

	@Override
	public synchronized int getInt(String key, int defValue) throws RemoteException {
		return prefs.getInt(key, defValue);
	}

	@Override
	public synchronized long getLong(String key, long defValue) throws RemoteException {
		return prefs.getLong(key, defValue);
	}

	@Override
	public synchronized float getFloat(String key, float defValue) throws RemoteException {
		return prefs.getFloat(key, defValue);
	}

	@Override
	public synchronized boolean getBoolean(String key, boolean defValue) throws RemoteException {
		return prefs.getBoolean(key, defValue);
	}

	@Override
	public synchronized boolean contains(String key) throws RemoteException {
		return prefs.contains(key);
	}

	@Override
	public synchronized boolean commit(ParcelableEditor editor) throws RemoteException {
		return getEditor(editor).commit();
	}

	@Override
	public synchronized void apply(ParcelableEditor editor) throws RemoteException {
		getEditor(editor).apply();
	}

	private final Map<IBinder, Listener> listeners = new HashMap<>();

	public synchronized void updateRemoteListeners(String key) {
		for (Listener listener : listeners.values())
			try {
				listener.listener.onSharedPreferenceChanged(key);
			} catch (RemoteException ignored) {}
	}

	private class Listener {
		RemoteOnSharedPreferenceChangeListener listener;
		DeathRecipient deathRecipient;
	}

	@Override
	public synchronized void registerOnSharedPreferenceChangeListener(RemoteOnSharedPreferenceChangeListener listener) throws RemoteException {
		final IBinder listenerBinder = listener.asBinder();

		if (!listeners.containsKey(listenerBinder)) {
			DeathRecipient dr = new DeathRecipient() {
				@Override
				public void binderDied() {
					unregister(listenerBinder);
				}
			};

			try {
				listenerBinder.linkToDeath(dr, 0);

				Listener wrapped = new Listener();
				wrapped.listener = listener;
				wrapped.deathRecipient = dr;

				listeners.put(listenerBinder, wrapped);
			} catch (RemoteException e) {
				// Client died, no cleanup needed.
			}
		}
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(RemoteOnSharedPreferenceChangeListener listener) throws RemoteException {
		final IBinder listenerBinder = listener.asBinder();
		unregister(listenerBinder);
	}

	private synchronized void unregister(IBinder listenerBinder) {
		Listener removed = listeners.remove(listenerBinder);
		IBinder binder = removed.listener.asBinder();
		if (binder != null)
			binder.unlinkToDeath(removed.deathRecipient, 0);
	}

	private SharedPreferences.Editor getEditor(ParcelableEditor editor) {
		@SuppressLint("CommitPrefEdits")
		SharedPreferences.Editor localEditor = prefs.edit();

		if (editor.mClear)
			localEditor.clear();

		for (Map.Entry<String, Object> modification : editor.mModified.entrySet()) {
			String key = modification.getKey();
			Object value = modification.getValue();

			if (value == null)
				localEditor.remove(key);
			else if (value instanceof Integer)
				localEditor.putInt(key, (Integer) value);
			else if (value instanceof Long)
				localEditor.putLong(key, (Long) value);
			else if (value instanceof String)
				localEditor.putString(key, (String) value);
			else if (value instanceof Boolean)
				localEditor.putBoolean(key, (Boolean) value);
			else if (value instanceof Set)
				localEditor.putStringSet(key, (Set<String>) value);
			else
				throw new RuntimeException("Unsupported type: " + value.getClass());
		}

		return localEditor;
	}

}
