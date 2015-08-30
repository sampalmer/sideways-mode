package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.IContentProviderSharedPreferenceChangeListener;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.OnSharedPreferenceChangeListenerContract;

public class UnregisterOnSharedPreferenceChangeListenerContract extends OnSharedPreferenceChangeListenerContract {

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
