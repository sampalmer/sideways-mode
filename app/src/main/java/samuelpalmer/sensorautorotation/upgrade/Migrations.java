package samuelpalmer.sensorautorotation.upgrade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.utilities.NumberUtilities;
import samuelpalmer.sensorautorotation.values.Orientation;
import samuelpalmer.sensorautorotation.values.UserSide;

import java.util.Set;

class Migrations {
	static SparseArray<Migration> buildMap() {
		SparseArray<Migration> migrations = new SparseArray<>();

		migrations.put(25, new Migration() {
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, prefs, editor);
				
				// Not migrating these prefs anywhere since the UI process (which owns the app prefs) can't directly access the service process's prefs as of v57.
				// Instead, we'll gracefully disable the app and restore normal Android rotation.
				if (prefs.contains("autoRotate") || prefs.contains("userOrientation")) {
					Log.w(Migrations.class.getSimpleName(), "App was enabled, but service state cannot be migrated to service process. Disabling app.");
					restoreSystemSettings(context, prefs.contains("autoRotate") ? prefs.getBoolean("autoRotate", true) : null);
				}
				
				editor
					.remove("autoRotate")
					.remove("userOrientation")
					.remove("screenOnBehaviour");
			}
		});

		migrations.put(43, new Migration() {
			@Override
			public void upgradeServiceProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeServiceProcessData(context, prefs, editor);
				
				editor.remove("userOrientation");
				
				if (prefs.contains("autoRotate") && prefs.contains("userOrientation") && prefs.getBoolean("autoRotate", true))
					//Not migrating this value when in locked orientation mode since it depends on the device orientation,
					// which we don't have, and the startup logic should be able to correctly calculate the new value anyway.
					editor.putInt("screenOrientationOffset", Orientation.NATURAL.minus(Orientation.fromOffset(prefs.getInt("userOrientation", 0))).offset);
			}
		});

		migrations.put(47, new Migration() {
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, prefs, editor);
				
				editor.remove("inferUserWhenDeviceLocked");
			}
		});
		
		migrations.put(51, new Migration() {
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, prefs, editor);
				
				//The format of this setting has changed.
				editor.remove("lastError");
			}
		});
		
		migrations.put(54, new Migration() {
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, prefs, editor);
				
				// This setting was removed since it exploited an Android OS bug, which might be considered a violation of Google Play terms regarding malicious behaviour.
				// See https://play.google.com/about/privacy-security/malicious-behavior/
				editor.remove("showNotification");
			}
		});
		
		migrations.put(57, new Migration() {
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, prefs, editor);
				
				editor
					.putBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, prefs.contains("lastVersionUserInstructedAbout"))
					.remove("allowUpsideDownScreen")
					.remove("notificationPriority")
					.remove("enabled") // This is now determined by the service process.
					.remove("lastError") // This setting has been moved into the service process.
					.remove("lastVersionUserInstructedAbout");
			}
			
			@Override
			public void upgradeServiceProcessData(Context context, SharedPreferences prefs, SharedPreferences.Editor editor) {
				super.upgradeServiceProcessData(context, prefs, editor);
				
				// The service process no longer has direct access to user preferences, so we'll have to ask the UI process to send over a copy.
				context.sendBroadcast(new Intent(context, SendConfigReceiver.class));
				
				Boolean autoRotateValue = prefs.contains("autoRotate") ? prefs.getBoolean("autoRotate", true) : null;
				Orientation screenOrientationOffsetValue = prefs.contains("screenOrientationOffset")
					?
					Orientation.fromOffset(NumberUtilities.wrap(prefs.getInt("screenOrientationOffset", 0), 4))
					:
					null;
				
				// We don't have access to the authoritative "enabled" setting from this process, so we'll just infer it.
				boolean wasEnabled = prefs.contains("autoRotate") || prefs.contains("screenOrientationOffset");
				if (wasEnabled) {
					if (
						autoRotateValue != null
						&&
						screenOrientationOffsetValue != null
						&&
						autoRotateValue
						&&
						(
							screenOrientationOffsetValue.equals(Orientation.LEFT_SIDE)
							||
							screenOrientationOffsetValue.equals(Orientation.RIGHT_SIDE)
						)
					) {
						UserSide newUserSide = screenOrientationOffsetValue.equals(Orientation.LEFT_SIDE) ? UserSide.LEFT : UserSide.RIGHT;
						editor.putString(ServiceSettings.KEY_PERSISTENT_STATE, newUserSide.name());
					}
					else {
						Log.w(Migrations.class.getSimpleName(), "App was enabled, but locked mode and upright position are no longer supported. Disabling app.");
						
						// The app doesn't support locked mode or being upright anymore, so we'll gracefully disable the app.
						restoreSystemSettings(context, autoRotateValue);
					}
				}
				
				editor.remove("autoRotate");
				editor.remove("screenOrientationOffset");
			}
		});
		
		migrations.put(61, new Migration() {
			@Override
			public void upgradeServiceProcessData(Context context, SharedPreferences serviceSettings, SharedPreferences.Editor editor) {
				super.upgradeServiceProcessData(context, serviceSettings, editor);
				
				Set<String> forcedOrientationAppPackages = serviceSettings.getStringSet("forcedOrientationAppPackages", null);
				if (forcedOrientationAppPackages != null)
					editor
						.putStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, forcedOrientationAppPackages)
						.remove("forcedOrientationAppPackages");
			}
			
			@Override
			public void upgradeUiProcessData(Context context, SharedPreferences applicationSettings, SharedPreferences.Editor editor) {
				super.upgradeUiProcessData(context, applicationSettings, editor);
				
				Set<String> forcedOrientationAppPackages = applicationSettings.getStringSet("forcedOrientationAppPackages", null);
				if (forcedOrientationAppPackages != null)
					editor
						.putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, forcedOrientationAppPackages)
						.remove("forcedOrientationAppPackages");
			}
		});

        migrations.put(64, new Migration() {
            @Override
            public void upgradeUiProcessData(Context context, SharedPreferences applicationSettings, SharedPreferences.Editor editor) {
                super.upgradeUiProcessData(context, applicationSettings, editor);

                boolean userInstructed = applicationSettings.getBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, false);
                if (userInstructed)
                    editor
                            .putBoolean(ApplicationSettings.KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN, true)
                            .putBoolean(ApplicationSettings.KEY_UNINSTALLING_TIP_SHOWN, true);
            }
        });

		return migrations;
	}

    private static void restoreSystemSettings(Context context, Boolean autoRotate) {
        if (!ScreenOrientationController.hasPermissionToStart(context))
			Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore the system settings while upgrading.");
		else {
			if (autoRotate != null)
				Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, autoRotate ? 1 : 0);
			Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
			// Note that it's possible for the above to fail due to DB write errors, but we'll just have to accept that and move on.
		}
	}
}
