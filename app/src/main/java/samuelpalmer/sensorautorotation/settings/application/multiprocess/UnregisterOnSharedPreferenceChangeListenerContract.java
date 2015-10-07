package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.SharedPreferences;

class UnregisterOnSharedPreferenceChangeListenerContract extends OnSharedPreferenceChangeListenerContract {

	@Override
	public String methodName() {
		return "unregisterOnSharedPreferenceChangeListener";
	}

	@Override
	public Void process(SharedPreferences sharedPreferences, IContentProviderSharedPreferenceChangeListener listener) {
		unsubscribe(sharedPreferences, listener.asBinder());
		return null;
	}

}
