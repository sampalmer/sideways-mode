package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContentProviderSharedPreferences implements SharedPreferences {

	private final Remote remote;

	public ContentProviderSharedPreferences(Context context, String contentProviderAuthority, String preferencesName) {
		remote = new Remote(context, contentProviderAuthority, preferencesName);
	}

	@Override
	public Map<String, ?> getAll() {
		return remote.process(new GetAllContract(), null);
	}

	@Override
	public String getString(String key, String defValue) {
		return remote.process(new GetStringContract(), new GetValueArgs<>(key, defValue));
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValues) {
		return remote.process(new GetStringSetContract(), new GetValueArgs<>(key, defValues));
	}

	@Override
	public int getInt(String key, int defValue) {
		return remote.process(new GetIntContract(), new GetValueArgs<>(key, defValue));
	}

	@Override
	public long getLong(String key, long defValue) {
		return remote.process(new GetLongContract(), new GetValueArgs<>(key, defValue));
	}

	@Override
	public float getFloat(String key, float defValue) {
		return remote.process(new GetFloatContract(), new GetValueArgs<>(key, defValue));
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return remote.process(new GetBooleanContract(), new GetValueArgs<>(key, defValue));
	}

	@Override
	public boolean contains(String key) {
		return remote.process(new ContainsContract(), key);
	}

	@Override
	public Editor edit() {
		return new MultiProcessEditor(remote);
	}

	private final HashMap<OnSharedPreferenceChangeListener, ClientListenerRecord> mListeners = new HashMap<>();

	public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
		synchronized(mListeners) {
			registerListenerLocked(listener);
		}
	}
	
	private void registerListenerLocked(final OnSharedPreferenceChangeListener listener) {
		final ClientListener client = new ClientListener(this, listener);
		
		IBinder serverBinder = remote.process(new RegisterOnSharedPreferenceChangeListenerContract(), client);
		IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
			@Override
			public void binderDied() {
				// No need to unlink to death since that the server process is already dead
				// No need to send an unsubscribe message to the server since the server died
				
				synchronized(mListeners) {
					mListeners.remove(listener);
					registerListenerLocked(listener);
				}
				
				// It's possible that the preference values changed since the binder died, so we'll update all subscribers.
				// TODO: This won't pick up preferences that have since been removed. Add support for that.
				for (String preferenceKey : ContentProviderSharedPreferences.this.getAll().keySet()) {
					try {
						client.onSharedPreferenceChanged(preferenceKey);
					} catch (RemoteException e) {
						throw new RuntimeException(e); // This should never happen since it's in the same process.
					}
				}
			}
		};
		
		ClientListenerRecord record = new ClientListenerRecord(client, serverBinder, deathRecipient);
		
		try {
			record.serverBinder.linkToDeath(record.deathRecipient, 0);
		} catch (RemoteException e) {
			// Server process died, so we'll try again
			// This could lead to infinite recursion...
			registerListenerLocked(listener);
			return;
		}
		
		mListeners.put(listener, record);
	}
	
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		synchronized(mListeners) {
			ClientListenerRecord record = mListeners.remove(listener);
			if (record != null) {
				record.serverBinder.unlinkToDeath(record.deathRecipient, 0);
				remote.process(new UnregisterOnSharedPreferenceChangeListenerContract(), record.listener);
			}
		}
	}

}
