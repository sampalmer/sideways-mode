package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcer;

public class ForcedAppsPermissionFragment extends Fragment {

	private static final int REQUEST_CODE_ACCESSIBILITY_SERVICE = 1;

	private AccessibilityServiceEnabledStateMonitor accessibilityServiceChecker;

	private PreferenceScreen preferenceScreen;
	private Context context; //Must use this context instead of getActivity() since the latter isn't always available soon enough
	private PermissionTipViewPreference tipView;

	public void initialise(PreferenceScreen preferenceScreen) {
		this.preferenceScreen = preferenceScreen;
		this.context = preferenceScreen.getContext();
		accessibilityServiceChecker = new AccessibilityServiceEnabledStateMonitor();

		tipView = (PermissionTipViewPreference) preferenceScreen.findPreference("forcedOrientationPermission");
		tipView.setOnGrantPermissionClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				grantPermissions();
			}
		});

		updateTip(); //Updating the visibility of the tip in onStart() isn't early enough when switching between split and fullscreen on Android 7.1
	}

	@Override
	public void onStart() {
		super.onStart();

		if (preferenceScreen == null)
			throw new IllegalStateException("Can't initialise because configure() hasn't been called");

		accessibilityServiceChecker.subscribe(serviceChanged);
		updateTip();
	}

	@Override
	public void onStop() {
		super.onStop();

		accessibilityServiceChecker.unsubscribe(serviceChanged);
	}

	private void grantPermissions() {
		//TODO: If the user is trying to disable the accessibility service on Android 7 or higher, just programmatically disable it.

		if (!accessibilityServiceChecker.isEnabled())
			new AlertDialog.Builder(context)
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ACCESSIBILITY_SERVICE)
			grantOverlayPermission();
	}

	private void grantOverlayPermission() {
		if (!ScreenOrientationEnforcer.hasPermissionToStart(context)) {
			new AlertDialog.Builder(context)
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
		if (accessibilityServiceChecker.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(context))
			preferenceScreen.removePreference(tipView);
		else
			preferenceScreen.addPreference(tipView);
	}

}
