package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class Settings {

	private static final String FILE_NAME = "samuelpalmer.sensorautorotation.prefs";
	private SharedPreferences prefs;
    private static final String ENABLED_PREF_NAME = "enabled";
	private OnSharedPreferenceChangeListener listener;
    
    public Settings(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
	}
    
	public boolean enabled() {
		return prefs.getBoolean(ENABLED_PREF_NAME, true);
	}

	public void setEnabled(boolean isChecked) {
		Editor editor = prefs.edit();
		editor.putBoolean(ENABLED_PREF_NAME, isChecked);
		editor.commit();
	}
	
	public void listen(final Listener listener) {
		if (this.listener != null)
			throw new RuntimeException("Already listening");
		
		this.listener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key == ENABLED_PREF_NAME)
					listener.tell();
			}
		};
		
		prefs.registerOnSharedPreferenceChangeListener(this.listener);
	}
	
	public void stopListening() {
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	public interface Listener {
		void tell();
	}
	
}
