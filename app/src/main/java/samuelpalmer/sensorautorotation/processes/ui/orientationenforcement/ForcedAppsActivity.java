package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;

import java.util.List;

import samuelpalmer.common.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;

//TODO: Keep the list of installed apps up-to-date (the user might install, uninstall, disable, or enable apps)
//TODO: Warn the user that apps aren't guaranteed to work correctly when using this mode
public class ForcedAppsActivity extends Activity {

	private Switch enabledSwitch;
	private AccessibilityManager accessibilityManager;
	private ProgressListView list;
	private LoadInstalledAppsTask loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accessibilityManager = SystemServices.accessibilityManager();

		setContentView(R.layout.forced_apps_activity);

		list = new ProgressListView();
		list.adapter = new InstalledAppAdapter(this);
		list.list = (ListView) findViewById(R.id.forced_apps_list);
		list.list.setAdapter(list.adapter);
		list.list.setItemsCanFocus(false); //Don't know if this is needed
		list.progressBar = (ProgressBar) findViewById(R.id.forced_apps_progress);

		loader = new LoadInstalledAppsTask(list);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loader.cancel(true);
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
			//TODO: Also warn the user that they might not be able to enable the accessibility service if a screen dimmer or other screen orientation controlling app is present
			//Note that it's possible that Samsung's app optimization feature might disable the accessibility service, although this might not occur since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272)
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
			ComponentName serviceName = ComponentNameExtensions.makeFrom(enabledService.getResolveInfo().serviceInfo);
			if (serviceName.equals(expectedName))
				return true;
		}

		return false;
	}
}
