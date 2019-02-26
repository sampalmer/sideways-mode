package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

public class IncompatibleAppsPermissionFragment extends Fragment {

	private static final int REQUEST_CODE_ACCESSIBILITY_SERVICE = 1;

	private ActivityMonitor activityMonitor;

	private PreferenceScreen preferenceScreen;
	private Context context; //Must use this context instead of getActivity() since the latter isn't always available soon enough
	private PermissionTipViewPreference tipView;

	public void initialise(PreferenceScreen preferenceScreen) {
		this.preferenceScreen = preferenceScreen;
		this.context = preferenceScreen.getContext();
		activityMonitor = ActivityMonitor.create(context);
		
		tipView = (PermissionTipViewPreference) preferenceScreen.findPreference("incompatibleAppsPermission");
		tipView.setOnPreferenceClickListener(preference -> {
			grantPermissions();
			return true;
		});
		
		updateTip(); //Updating the visibility of the tip in onStart() isn't early enough when switching between split and fullscreen on Android 7.1
	}
	
	@Override
	public void onStart() {
		super.onStart();

		if (preferenceScreen == null)
			throw new IllegalStateException("Can't initialise because configure() hasn't been called");
		
		Preference description = preferenceScreen.findPreference("incompatibleAppsDetails");
		description.setSummary(getString(R.string.preference_incompatible_apps_details, getText(R.string.app_name_full)));
		
		activityMonitor.onPermissionChange().subscribe(permissionChanged);
		updateTip();
	}

	@Override
	public void onStop() {
		super.onStop();

		activityMonitor.onPermissionChange().unsubscribe(permissionChanged);
	}

	private void grantPermissions() {
		if (!activityMonitor.hasPermission())
			new AlertDialog.Builder(context)
					.setTitle(activityMonitor.makeAcquirePermissionQuestion())
					.setMessage(activityMonitor.makeAcquirePermissionExplanation())
					.setOnCancelListener(dialog -> {})
					.setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
					.setPositiveButton(R.string.permission_needed_allow, (dialog, which) -> {
						Intent intent = activityMonitor.makeAcquirePermissionIntent();
						startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY_SERVICE);
					})
					.show();
		else
			grantOverlayPermission();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ACCESSIBILITY_SERVICE) {
			if (activityMonitor.hasPermission())
				grantOverlayPermission();
			else {
				// Maybe the user couldn't enable the accessibility service due to an overlay.
				// On some versions of Android, the OS doesn't tell you why you can't turn it on.
				Toast
					.makeText(context, R.string.accessibility_service_cancelled, Toast.LENGTH_LONG)
					.show();
			}
		}
	}

	private void grantOverlayPermission() {
		if (!ScreenOrientationEnforcer.hasPermissionToStart(context)) {
			new AlertDialog.Builder(context)
					.setTitle(R.string.permission_needed_overlays_summary)
					.setMessage(getString(R.string.permission_needed_overlays_description, getText(R.string.app_name_full)))
					.setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
					.setPositiveButton(R.string.permission_needed_allow, (dialog, which) -> {
						Intent intent = SystemIntents.drawOnTopOfApps(getActivity());
						startActivity(intent);
					})
					.show();
		}
	}

	private final EventHandler permissionChanged = this::updateTip;

	private void updateTip() {
		if (activityMonitor.hasPermission() && ScreenOrientationEnforcer.hasPermissionToStart(context))
			preferenceScreen.removePreference(tipView);
		else
			preferenceScreen.addPreference(tipView);
	}

}
