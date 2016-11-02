package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;
import samuelpalmer.sensorautorotation.processes.ui.Email;

//TODO: Keep the list of installed apps up-to-date (the user might install or uninstall apps)
public class ForcedAppsActivity extends Activity {

	private Switch enabledSwitch;
	private AccessibilityManager accessibilityManager;
	private PackageManager packageManager;
	private ArrayAdapter<InstalledApp> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accessibilityManager = SystemServices.accessibilityManager();
		packageManager = SystemServices.packageManager();
		adapter = new InstalledAppAdapter(this);

		setContentView(R.layout.forced_apps_activity);

		ListView list = (ListView) findViewById(R.id.forced_apps_list);
		list.setAdapter(adapter);
		list.setItemsCanFocus(false); //Don't know if this is needed

		//TODO: Hook the listview up to the preference

		//TODO: Do this in background and show a loading indicator
		ArrayList<InstalledApp> installedApps = lookupOptions();
		adapter.addAll(installedApps);
	}

	private ArrayList<InstalledApp> lookupOptions() {
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

		ArrayList<InstalledApp> options = new ArrayList<>();
		for (PackageInfo installedPackage : installedPackages)
			if (shouldShow(installedPackage))
				options.add(new InstalledApp(installedPackage, packageManager));

		Collections.sort(options, new InstalledAppComparator());
		return options;
	}

	private boolean shouldShow(PackageInfo installedPackage) {
		return
			installedPackage.activities != null
			&&
			installedPackage.activities.length > 0
			&&
			installedPackage.applicationInfo != null
			&&
			installedPackage.applicationInfo.enabled;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.forced_apps, menu);

		enabledSwitch = (Switch) menu.findItem(R.id.forced_apps_switch_menu_item).getActionView().findViewById(R.id.forced_apps_switch);
		enabledSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
		updateSwitchState();

		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateSwitchState(); //Maybe the accessibility service was enabled or disabled since this activity was last visible.
	}

	private void updateSwitchState() {
		if (enabledSwitch != null) {
			enabledSwitch.setOnCheckedChangeListener(null);
			enabledSwitch.setChecked(isEnabled());
			enabledSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
		}
	}

	private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			buttonView.setChecked(!isChecked); //Change it back; don't want the user to be able to directly change the value
			//TODO: Instruct the user about what to do before sending them to the accessibility settings
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

			try {
				startActivity(intent);
			}
			catch (ActivityNotFoundException ex) {
				//TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
				throw ex;
			}
		}
	};

	private boolean isEnabled() {
		ComponentName expectedName = new ComponentName(this, WindowChangeDetectingService.class);

		//TODO: Put this on StackOverflow; it seems simpler and more reliable than the answers already out there
		List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
		for (AccessibilityServiceInfo enabledService : enabledServices) {
			ComponentName serviceName = Email.makeComponentName(enabledService.getResolveInfo().serviceInfo);
			if (serviceName.equals(expectedName))
				return true;
		}

		return false;
	}
}
