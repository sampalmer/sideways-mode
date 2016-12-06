package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.preferences.RemotePreferencesActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ForcedAppsActivity extends RemotePreferencesActivity {

	private AccessibilityServiceEnabledStateMonitor accessibilityServiceChecker;
	private View tipView;
	private TextView tip;
	private Settings.Setting<Set<String>> forcedApps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.forced_apps_activity);

		accessibilityServiceChecker = new AccessibilityServiceEnabledStateMonitor();
		forcedApps = ApplicationSettings.remote().forcedOrientationAppPackages();
		tipView = findViewById(R.id.forced_apps_tip_view);
		tip = (TextView) findViewById(R.id.conversation_tip_text);
		Button enableAccessibilityServiceButton = (Button) findViewById(R.id.forced_apps_accessibility_service_enable);

		enableAccessibilityServiceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				enableAccessibilityService();
			}
		});

		updateTip();
	}

	@Override
	protected void onStart() {
		super.onStart();

		accessibilityServiceChecker.subscribe(serviceChanged);
		forcedApps.subscribe(serviceChanged);
		updateTip();
	}

	@Override
	protected void onStop() {
		super.onStop();

		forcedApps.unsubscribe(serviceChanged);
		accessibilityServiceChecker.unsubscribe(serviceChanged);
	}

	private void enableAccessibilityService() {
		//TODO: If the user is trying to disable the accessibility service on Android 7 or higher, just programmatically disable it.

		new AlertDialog.Builder(this)
				.setTitle(R.string.preference_accessibility_service_summary)
				.setMessage(StringResource.format(R.string.accessibility_service_prompt_instruction, R.string.accessibility_service_name))
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						updateTip();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

						try {
							startActivity(intent);
						}
						catch (ActivityNotFoundException ex) {
							//TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
							throw ex;
						}
					}
				})
				.show();
	}

	private final EventHandler serviceChanged = new EventHandler() {
		@Override
		public void update() {
			updateTip();
		}
	};

	private void updateTip() {
		if (accessibilityServiceChecker.isEnabled())
			tipView.setVisibility(View.GONE);
		else {
			CharSequence tipMessage = StringResource.get(R.string.accessibility_service_tip);

			if (!forcedApps.get().isEmpty())
				tipMessage = StringResource.get(R.string.warning_accessibility_service).toString() + " " + tipMessage.toString();

			tip.setText(tipMessage);
			tipView.setVisibility(View.VISIBLE);
		}
	}

}
