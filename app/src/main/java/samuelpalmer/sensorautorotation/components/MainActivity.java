package samuelpalmer.sensorautorotation.components;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ToggleButton;

import java.util.List;

import samuelpalmer.sensorautorotation.R;

public class MainActivity extends ActionBarActivity {

	private ToggleButton serviceEnabledButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serviceEnabledButton = (ToggleButton) findViewById(R.id.serviceEnabledButton);
		serviceEnabledButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateEnabledButtonState(); //Prevents button from toggling its own state.
				showAccessibilitySettings();
			}
		});

		updateEnabledButtonState();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ensureUserInstructed();
	}

	private void ensureUserInstructed() {
		samuelpalmer.sensorautorotation.Settings.Setting<Boolean> userInstructed = new samuelpalmer.sensorautorotation.Settings(this).userInstructedAboutAccessibilityService();
		if (!(userInstructed.getOr(false) || serviceEnabled())) {
			new AlertDialog.Builder(this)
					.setCancelable(true)
					.setMessage("This app runs as an accessibility service. To use it, you must enable it in the accessibility settings. Do you want to do this now?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showAccessibilitySettings();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					})
					.create()
					.show();
		}

		userInstructed.set(true);
	}

	private void showAccessibilitySettings() {
		Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
		startActivity(intent);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		updateEnabledButtonState();
	}

	private void updateEnabledButtonState() {
		serviceEnabledButton.setChecked(serviceEnabled());
	}

	private boolean serviceEnabled() {
		AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> enabledAccessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

		for (AccessibilityServiceInfo enabledService : enabledAccessibilityServices) {
			String enabledServiceName = enabledService.getResolveInfo().serviceInfo.name;
			if (enabledServiceName.equals(RotationService.class.getName()))
				return true;
		}

		return false;
	}
}
