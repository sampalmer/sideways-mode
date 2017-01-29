package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcer;

public class ForcedAppsActivity extends PreferencesActivity {

	private static final int REQUEST_CODE_ACCESSIBILITY_SERVICE = 1;

	private AccessibilityServiceEnabledStateMonitor accessibilityServiceChecker;
	private View tipView;
	private TextView tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.forced_apps_activity);

		accessibilityServiceChecker = new AccessibilityServiceEnabledStateMonitor();
		tipView = findViewById(R.id.forced_apps_tip_view);
		tip = (TextView) findViewById(R.id.conversation_tip_text);
		Button grantPermissionsButton = (Button) findViewById(R.id.forced_apps_permissions_enable);

		grantPermissionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				grantPermissions();
			}
		});

		updateTip();
	}

	@Override
	protected void onStart() {
		super.onStart();

		accessibilityServiceChecker.subscribe(serviceChanged);
		updateTip();
	}

	@Override
	protected void onStop() {
		super.onStop();

		accessibilityServiceChecker.unsubscribe(serviceChanged);
	}

	private void grantPermissions() {
		//TODO: If the user is trying to disable the accessibility service on Android 7 or higher, just programmatically disable it.

		if (!accessibilityServiceChecker.isEnabled())
			new AlertDialog.Builder(this)
					.setTitle(R.string.preference_accessibility_service_summary)
					.setMessage(StringResource.format(R.string.accessibility_service_prompt_instruction, R.string.accessibility_service_name))
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							updateTip(); //TODO: This looks redundant; consider removing it
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

							try {
								startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY_SERVICE);
							}
							catch (ActivityNotFoundException ex) {
								//TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
								throw ex;
							}
						}
					})
					.show();
		else
			grantOverlayPermission();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ACCESSIBILITY_SERVICE)
			grantOverlayPermission();
	}

	private void grantOverlayPermission() {
		if (!ScreenOrientationEnforcer.hasPermissionToStart(this)) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.preference_draw_overlays_summary)
					.setMessage(R.string.environment_error_permission_needed_draw_overlays_message)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@TargetApi(Build.VERSION_CODES.M)
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

							try {
								startActivity(intent);
							} catch (ActivityNotFoundException ex) {
								//TODO: Apparently the activity isn't guaranteed to actually exist. Decide what to do here.
								throw ex;
							}
						}
					})
					.show();
		}
	}

	private final EventHandler serviceChanged = new EventHandler() {
		@Override
		public void update() {
			updateTip();
		}
	};

	private void updateTip() {
		if (accessibilityServiceChecker.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(this))
			tipView.setVisibility(View.GONE);
		else {
			CharSequence tipMessage = StringResource.get(R.string.permissions_needed_tip);

			tip.setText(tipMessage);
			tipView.setVisibility(View.VISIBLE);
		}
	}

}
