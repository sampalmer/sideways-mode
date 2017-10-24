package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.utilities.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

public class ForcedAppsPermissionFragment extends Fragment {

	private static final int REQUEST_CODE_ACCESSIBILITY_SERVICE = 1;

	private AccessibilityServiceEnabledStateMonitor accessibilityServiceChecker;

	private PreferenceScreen preferenceScreen;
	private Context context; //Must use this context instead of getActivity() since the latter isn't always available soon enough
	private PermissionTipViewPreference tipView;

	public void initialise(PreferenceScreen preferenceScreen) {
		this.preferenceScreen = preferenceScreen;
		this.context = preferenceScreen.getContext();
		accessibilityServiceChecker = new AccessibilityServiceEnabledStateMonitor(getActivity(), WindowChangeDetectingService.class);

		tipView = (PermissionTipViewPreference) preferenceScreen.findPreference("forcedOrientationPermission");
		tipView.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				grantPermissions();
				return true;
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
		if (!accessibilityServiceChecker.isEnabled())
			new AlertDialog.Builder(context)
					.setTitle(R.string.permission_needed_accessibility_service_summary)
					.setMessage(getString(R.string.permission_needed_accessibility_service_description, getText(R.string.app_name_full)))
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

							//TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
							startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY_SERVICE);
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
					.setTitle(R.string.permission_needed_overlays_summary)
					.setMessage(R.string.permission_needed_overlays_description)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@TargetApi(Build.VERSION_CODES.M)
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = SystemIntents.drawOnTopOfApps(getActivity());

							//TODO: Apparently the activity isn't guaranteed to actually exist. Decide what to do here.
							startActivity(intent);
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
