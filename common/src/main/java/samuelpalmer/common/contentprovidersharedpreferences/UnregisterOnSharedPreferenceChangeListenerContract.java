package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.SharedPreferences;
import android.os.Bundle;

class UnregisterOnSharedPreferenceChangeListenerContract extends OnSharedPreferenceChangeListenerContract<Void> {

	@Override
	public String methodName() {
		return "unregisterOnSharedPreferenceChangeListener";
	}

	@Override
	public Void process(SharedPreferences sharedPreferences, IContentProviderSharedPreferenceChangeListener listener) {
		unsubscribe(sharedPreferences, listener.asBinder());
		return null;
	}
	
	public Bundle serialiseResult(Void value) {
		return new Bundle();
	}
	
	public Void deserialiseResult(Bundle serialised) {
		return null;
	}
	
}
