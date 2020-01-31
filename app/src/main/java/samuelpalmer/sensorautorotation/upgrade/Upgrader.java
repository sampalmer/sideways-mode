package samuelpalmer.sensorautorotation.upgrade;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

import java.util.Map;

public class Upgrader {
    
    /**
     * The last version of this app that was run. Intended for upgrade-related logic. Note that this
     * is only available after {@link #upgradeIfNecessary(Context)} has been called.
     * @return The {@link android.content.pm.PackageInfo#versionCode} of the previously-run version of this app, or {@code null} if none exists.
     */
    public static Integer getLastAppVersion() {
        if (!lastAppVersionSet)
            throw new RuntimeException("The last app version hasn't yet been determined. Please try after upgrading the settings.");
        
        return lastAppVersion;
    }
    
    private static Integer lastAppVersion;
    private static boolean lastAppVersionSet = false;
    
    public static void upgradeIfNecessary(Context context) {
        if (lastAppVersionSet)
            throw new RuntimeException("This has already been called.");

        int newAppVersion = BuildConfig.VERSION_CODE;

        SharedPreferences prefs;
        String settingsAppVersionKey;
        
        if (CurrentProcess.isServiceProcess(context)) {
            prefs = ServiceSettings.getSharedPreferences(context);
            settingsAppVersionKey = ServiceSettings.KEY_APP_VERSION;
            lastAppVersion = prefs.contains(settingsAppVersionKey) ? prefs.getInt(settingsAppVersionKey, -1) : null;
            
            if (lastAppVersion == null) {
                // Either the app is newly installed or the user was using a version prior to 57, in which the app version wasn't stored in the service settings.
                lastAppVersion = guessLastAppVersion(prefs);
                if (lastAppVersion != null)
                    Log.i(Upgrader.class.getSimpleName(), "Guessing that app was previously on version " + lastAppVersion);
            }
        }
        else {
            prefs = ApplicationSettings.getSharedPreferences(context);
            settingsAppVersionKey = ApplicationSettings.KEY_APP_VERSION;
            lastAppVersion = prefs.contains(settingsAppVersionKey) ? prefs.getInt(settingsAppVersionKey, -1) : null;
            
            if (lastAppVersion == null)
                // The settings app version was stored in a different setting prior to v57.
                lastAppVersion = prefs.contains("lastVersionUserInstructedAbout") ? prefs.getInt("lastVersionUserInstructedAbout", -1) : null;
        }
        lastAppVersionSet = true;
        
        if (
            lastAppVersion != null // We only need to upgrade the app if it has been run before.
            &&
            lastAppVersion != newAppVersion
        ) {
            Log.i(Upgrader.class.getSimpleName(), "Upgrading " + (CurrentProcess.isServiceProcess(context) ? "service" : "application") + " settings from " + lastAppVersion + " to " + newAppVersion);
            
            if (lastAppVersion > newAppVersion) {
                // On my Sony Xperia M running Android 4.1.2, the OS lets me downgrade the app, so we need to guard against this.
                
                Log.w(Upgrader.class.getSimpleName(), "Warning: user is downgrading from a later version. Clearing settings.");
                
                prefs.edit().clear().apply();
                
                if (CurrentProcess.isServiceProcess(context)) {
                    if (!ScreenOrientationController.hasPermissionToStart(context))
                        Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore screen orientation while downgrading.");
                    else {
                        Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                        // Note that it's possible for the above to fail due to DB write errors, but we'll just have to accept that and move on.
                    }
                }
            }
            else {
                SparseArray<Migration> migrations = Migrations.buildMap();
                for (int version = lastAppVersion + 1; version <= newAppVersion; ++version) {
                    Migration migration = migrations.get(version);
                    if (migration != null) {
                        Log.i(Upgrader.class.getSimpleName(), "Running upgrade script for version " + version);
                        
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        if (CurrentProcess.isServiceProcess(context))
                            migration.upgradeServiceProcessData(context, prefs, editor);
                        else
                            migration.upgradeUiProcessData(context, prefs, editor);
                        
                        // Keeping the settings version in sync with the upgrade scripts in case the app crashes or is killed part-way through.
                        editor.putInt(settingsAppVersionKey, version);
                        editor.apply();
                    }
                }
            }
            
            prefs.edit().putInt(settingsAppVersionKey, newAppVersion).apply();
        }
        else if (lastAppVersion == null)
            // So that we know the settings version when we next upgrade.
            prefs.edit().putInt(settingsAppVersionKey, newAppVersion).apply();
    }
    
    /**
     * App versions prior to 57 didn't store the app version number in the service process's preferences.
     * This method guesses the version based on the contents of the settings.
     */
    private static Integer guessLastAppVersion(SharedPreferences serviceSettings) {
        Map<String, ?> allPreferences = serviceSettings.getAll();
        
        if (!allPreferences.isEmpty()) {
            if (
                allPreferences.size() == 2
                &&
                allPreferences.get("autoRotate") instanceof Boolean
                &&
                allPreferences.get("screenOrientationOffset") instanceof Integer
            )
                return 43;
            else if (
                allPreferences.size() == 2
                &&
                allPreferences.get("autoRotate") instanceof Boolean
                &&
                allPreferences.get("userOrientation") instanceof Integer
            )
                return 25;
        }
        
        return null;
    }
    
}
