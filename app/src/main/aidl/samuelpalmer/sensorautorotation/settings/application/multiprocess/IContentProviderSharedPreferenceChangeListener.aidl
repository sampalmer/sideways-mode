// IContentProviderSharedPreferenceChangeListener.aidl
package samuelpalmer.sensorautorotation.settings.application.multiprocess;

// Declare any non-default types here with import statements

interface IContentProviderSharedPreferenceChangeListener {
    void onSharedPreferenceChanged(String key);
}
