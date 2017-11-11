package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.annotation.SuppressLint;
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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.IntentExtensions;

/**
 * Hosts the main settings.
 */
public class MainSettingsFragment extends PreferenceFragment {
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getPreferenceManager().setSharedPreferencesName(ApplicationSettings.getFileName(getActivity()));
		
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
			
			if (IntentExtensions.activityDoesntExist(assistSettingsIntent, getActivity().getPackageManager()))
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
	@SuppressLint("PrivateApi") // The app is designed to tolerate the private APIs being unavailable
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
	
}
