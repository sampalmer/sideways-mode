package samuelpalmer.sensorautorotation.settings.application.remote;

import android.content.SharedPreferences;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoteSharedPreferencesAdaptor implements SharedPreferences {

	private final RemoteSharedPreferences remote;

	public RemoteSharedPreferencesAdaptor(RemoteSharedPreferences remote) {
		this.remote = remote;
	}

	@Override
	public Map<String, ?> getAll() {
		try {
			return (Map<String, ?>) remote.getAll();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getString(String key, String defValue) {
		try {
			return remote.getString(key, defValue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValues) {
		try {
			String[] defValuesArray = defValues.toArray(new String[defValues.size()]);
			String[] resultArray = remote.getStringSet(key, defValuesArray);
			List<String> resultCollection = Arrays.asList(resultArray);
			return new HashSet<>(resultCollection);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(String key, int defValue) {
		try {
			return remote.getInt(key, defValue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(String key, long defValue) {
		try {
			return remote.getLong(key, defValue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(String key, float defValue) {
		try {
			return remote.getFloat(key, defValue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		try {
			return remote.getBoolean(key, defValue);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean contains(String key) {
		try {
			return remote.contains(key);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Editor edit() {
		return new RemoteEditorAdaptor(remote);
	}

	@Override
	public synchronized void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		OnSharedPreferenceChangedListenerAdaptor remoteListener = new OnSharedPreferenceChangedListenerAdaptor(listener);

		try {
			remote.registerOnSharedPreferenceChangeListener(remoteListener);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

		listeners.put(listener, remoteListener);
	}

	@Override
	public synchronized void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		OnSharedPreferenceChangedListenerAdaptor remoteListener = listeners.remove(listener);
		if (remoteListener != null)
			try {
				remote.unregisterOnSharedPreferenceChangeListener(remoteListener);
			} catch (DeadObjectException ignored) {
				//The service has died, so it doesn't matter that unregistration failed
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
	}

	private final Map<OnSharedPreferenceChangeListener, OnSharedPreferenceChangedListenerAdaptor> listeners = new HashMap<>();

	private class OnSharedPreferenceChangedListenerAdaptor extends RemoteOnSharedPreferenceChangeListener.Stub {
		private final OnSharedPreferenceChangeListener listener;

		public OnSharedPreferenceChangedListenerAdaptor(OnSharedPreferenceChangeListener listener) {
			this.listener = listener;
		}

		@Override
		public void onSharedPreferenceChanged(final String key) throws RemoteException {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					listener.onSharedPreferenceChanged(RemoteSharedPreferencesAdaptor.this, key);
				}
			});
		}
	}
}
