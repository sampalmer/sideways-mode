package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.ContainsContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetAllContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetBooleanContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetFloatContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetIntContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetLongContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetStringContract;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts.GetStringSetContract;

public class ContentProviderSharedPreferences implements SharedPreferences {

	private final Remote remote;

	public ContentProviderSharedPreferences(String name) {
		remote = new Remote(name);
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

	private final WeakHashMap<OnSharedPreferenceChangeListener, ClientListener> mListeners = new WeakHashMap<>();

	public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
		synchronized(this) {
			//SUBSCRIBE
			mListeners.put(listener, new ClientListener(this, listener));
		}
	}

	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		synchronized(this) {
			//UNSUBSCRIBE
			mListeners.remove(listener);
		}
	}

}
