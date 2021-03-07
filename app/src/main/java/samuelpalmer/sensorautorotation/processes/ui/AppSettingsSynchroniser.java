package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Automatically pushes changes to user preferences from the UI process to the service process.
 * This ensures preference changes made by the user appear to instantly take effect.
 */
public class AppSettingsSynchroniser extends State {

    private static final String TAG = AppSettingsSynchroniser.class.getSimpleName();

    private final Context context;
    private final SharedPreferences applicationSettings;

    public AppSettingsSynchroniser(Context context) {
        this.context = context;
        applicationSettings = ApplicationSettings.getSharedPreferences(context);
    }

    @Override
    protected void start() {
        applicationSettings.registerOnSharedPreferenceChangeListener(applicationSettingsChanged);
    }

    @Override
    protected void stop() {
        applicationSettings.unregisterOnSharedPreferenceChangeListener(applicationSettingsChanged);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener applicationSettingsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == null || ApplicationCommand.Config.isSyncableAppSetting(key)) {
                Log.i(TAG, "User preferences changed. Sending to service process.");

                new ApplicationCommand(
                        ApplicationCommand.ACTION_SYNC_CONFIG,
                        applicationSettings
                ).send(context);
            }
        }

    };

}
