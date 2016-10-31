package samuelpalmer.sensorautorotation.processes.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;

//TODO: Somehow get the installed apps to show up in here
public class ForcedAppsActivity extends Activity {

	private Switch enabledSwitch;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.forced_apps, menu);

		enabledSwitch = (Switch) menu.findItem(0).getActionView().findViewById(R.id.forced_apps_switch);
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
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS); //TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist
			startActivity(intent);
		}
	};

	private boolean isEnabled() {
		ComponentName expectedName = new ComponentName(this, WindowChangeDetectingService.class);

		//TODO: Put this on StackOverflow; it seems simpler and more reliable than the answers already out there
		List<AccessibilityServiceInfo> enabledServices = SystemServices.accessibilityManager().getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
		for (AccessibilityServiceInfo enabledService : enabledServices) {
			ComponentName serviceName = Email.makeComponentName(enabledService.getResolveInfo().serviceInfo);
			if (serviceName.equals(expectedName))
				return true;
		}

		return false;
	}
}
