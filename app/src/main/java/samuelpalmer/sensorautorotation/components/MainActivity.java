package samuelpalmer.sensorautorotation.components;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
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
				Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

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
