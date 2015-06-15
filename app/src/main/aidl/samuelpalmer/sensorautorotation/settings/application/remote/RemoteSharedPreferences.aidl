// RemoteSharedPreferences.aidl
package samuelpalmer.sensorautorotation.settings.application.remote;

// Declare any non-default types here with import statements
import samuelpalmer.sensorautorotation.settings.application.remote.ParcelableEditor;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteOnSharedPreferenceChangeListener;

interface RemoteSharedPreferences {
    Map getAll();
    String getString(String key, String defValue);
    String[] getStringSet(String key, in String[] defValues);
    int getInt(String key, int defValue);
    long getLong(String key, long defValue);
    float getFloat(String key, float defValue);
    boolean getBoolean(String key, boolean defValue);
    boolean contains(String key);

	boolean commit(in ParcelableEditor editor);
    void apply(in ParcelableEditor editor);

    void registerOnSharedPreferenceChangeListener(RemoteOnSharedPreferenceChangeListener listener);
    void unregisterOnSharedPreferenceChangeListener(RemoteOnSharedPreferenceChangeListener listener);
}
