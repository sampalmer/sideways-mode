package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import samuelpalmer.common.IntentExtensions;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.settings.application.Settings;
import samuelpalmer.sensorautorotation.settings.application.SettingsInitialiser;

public class MainSettingsFragment extends PreferenceFragment {
	
	private Preference forcedAppsPreference;
	private AccessibilityServiceEnabledStateMonitor accessibilityServiceEnabledStateMonitor;
	private Settings.Setting<Set<String>> forcedApps;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getPreferenceManager().setSharedPreferencesName(ApplicationSettings.sharedPreferencesName());
		
		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		new SettingsInitialiser(ApplicationSettings.instance()).ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);
		
		Preference quickSettingPreference = findPreference("quick_setting");
		quickSettingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.preference_quick_setting)
					.setMessage(R.string.preference_quick_setting_instructions)
					.show();
				
				return true;
			}
		});
		if (Build.VERSION.SDK_INT < 24)
			removePreference(quickSettingPreference);
		
		Preference assistPreference = findPreference("assist");
		if (Build.VERSION.SDK_INT >= 23) {
			final Intent assistSettingsIntent = new Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS);
			
			if (IntentExtensions.activityDoesntExist(assistSettingsIntent, CurrentProcess.context().getPackageManager()))
				removePreference(assistPreference);
			else
				assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						new AlertDialog.Builder(getActivity())
							.setTitle(R.string.preference_assist)
							.setMessage(R.string.preference_assist_instructions_lollipop)
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							})
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@TargetApi(Build.VERSION_CODES.LOLLIPOP)
								@Override
								public void onClick(DialogInterface dialog, int which) {
									try {
										startActivity(assistSettingsIntent);
									}
									catch (ActivityNotFoundException e) {
										throw new RuntimeException("Couldn't find assist settings activity. The user shouldn't have been able to get this far.");
									}
								}
							})
							.show();
						
						return true;
					}
				});
		}
		else if (Build.VERSION.SDK_INT >= 16)
			if (hasNavigationBar() == Boolean.FALSE)
				removePreference(assistPreference);
			else
				assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						new AlertDialog.Builder(getActivity())
							.setTitle(R.string.preference_assist)
							.setMessage(R.string.preference_assist_instructions)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							})
							.show();
						
						return true;
					}
				});
		else
			removePreference(assistPreference);
		
		Preference smallAppPreference = findPreference("small_app");
		smallAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.preference_small_app)
					.setMessage(R.string.preference_small_app_instructions)
					.show();
				
				return true;
			}
		});
		if (!supportsSmallApp())
			removePreference(smallAppPreference);
		
		PreferenceGroup controlsSection = (PreferenceGroup) findPreference("controls");
		if (controlsSection.getPreferenceCount() == 0)
			removePreference(controlsSection);
		
		//TODO: move all of this preference summary updating stuff into its own class
		forcedApps = ApplicationSettings.instance().forcedOrientationAppPackages();
		forcedAppsPreference = findPreference(forcedApps.getKey());
		accessibilityServiceEnabledStateMonitor = new AccessibilityServiceEnabledStateMonitor();
	}
	
	private void removePreference(Preference preference) {
		PreferenceGroup parent = getParent(getPreferenceScreen(), preference);
		if (parent == null)
			throw new RuntimeException("Couldn't find preference");
		
		parent.removePreference(preference);
	}
	
	private PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
		for (int i = 0; i < groupToSearchIn.getPreferenceCount(); ++i) {
			Preference child = groupToSearchIn.getPreference(i);
			
			if (child == preference)
				return groupToSearchIn;
			
			if (child instanceof PreferenceGroup) {
				PreferenceGroup childGroup = (PreferenceGroup)child;
				PreferenceGroup result = getParent(childGroup, preference);
				if (result != null)
					return result;
			}
		}
		
		return null;
	}
	
	private static boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns {@code null} if this couldn't be determined.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static Boolean hasNavigationBar() {
		try {
			Class<?> serviceManager = Class.forName("android.os.ServiceManager");
			IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
			Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
			Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
			Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
			return (boolean)hasNavigationBar.invoke(windowManagerService);
		} catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.w(MainSettingsFragment.class.getSimpleName(), "Couldn't determine whether the device has a navigation bar", e);
			return null;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		accessibilityServiceEnabledStateMonitor.subscribe(forcedAppsChanged);
		forcedApps.subscribe(forcedAppsChanged);
		updateForcedApps();
	}

	@Override
	public void onStop() {
		super.onStop();

		forcedApps.unsubscribe(forcedAppsChanged);
		accessibilityServiceEnabledStateMonitor.unsubscribe(forcedAppsChanged);
	}
	
	private final EventHandler forcedAppsChanged = new EventHandler() {
		@Override
		public void update() {
			updateForcedApps();
		}
	};

	private void updateForcedApps() {
		final int quantity = forcedApps.get().size();

		if (quantity > 0 && !(accessibilityServiceEnabledStateMonitor.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(getActivity())))
			forcedAppsPreference.setSummary(R.string.permission_needed_summary);
		else {
			final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
			forcedAppsPreference.setSummary(quantityString);
		}
	}
	
}
