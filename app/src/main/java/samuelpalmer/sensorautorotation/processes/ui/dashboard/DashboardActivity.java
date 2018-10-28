package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.InstructionsActivity;
import samuelpalmer.sensorautorotation.processes.ui.Uninstaller;
import samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps.IncompatibleAppsActivity;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;

public class DashboardActivity extends DialogActivity {

	public static final String TAG_TIPS_FRAGMENT = "tips";
	public AlertDialog dialog;
	
	private DashboardNormalFragment normalSection;
	private DashboardPermissionFragment permissionSection;
	private FirstStartTips tipsFragment;
	private ImageButton menuButton;
	private PopupMenu popup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = new AlertDialog.Builder(this)
			.setView(R.layout.activity_dashboard)
			.create();
		
		// It's important that we finish the activity when the dialog is *cancelled*, not dismissed.
		// This is because dismissal also occurs when the screen orientation or layout changes.
		// Hence if we finished the activity on *dismiss*, the dialog would disappear each time the screen orientation changes.
		dialog.setOnCancelListener(dialogInterface -> {
			dialog = null;
			finish();
		});
		dialog.show();

		menuButton = dialog.findViewById(R.id.dashboard_menu);
		assert menuButton != null;
		menuButton.setOnClickListener(v -> popup.show());

		popup = new PopupMenu(this, menuButton);
        popup.setOnMenuItemClickListener(this::handleMenuItemClick);
        popup.inflate(R.menu.main);

		FragmentManager fragmentManager = getSupportFragmentManager();

		normalSection = (DashboardNormalFragment) fragmentManager.findFragmentById(R.id.dashboard_normal_contents);
		permissionSection = (DashboardPermissionFragment) fragmentManager.findFragmentById(R.id.dashboard_permission_contents);

		tipsFragment = (FirstStartTips) fragmentManager.findFragmentByTag(TAG_TIPS_FRAGMENT);
		if (tipsFragment == null) {
			tipsFragment = new FirstStartTips();
			fragmentManager
				.beginTransaction()
				.add(tipsFragment, TAG_TIPS_FRAGMENT)
				.commit();
		}

		tipsFragment.setTargets(dialog, normalSection.getSideButton(), popup);

		normalSection.setStatusChangeListener(status -> {
			if (status != null)
				tipsFragment.x();
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		boolean hasPermission = SystemSettings.canWrite(this);
		ViewUtilities.ensureVisibility(normalSection.getView(), hasPermission ? View.VISIBLE : View.GONE);
		ViewUtilities.ensureVisibility(permissionSection.getView(), hasPermission ? View.GONE : View.VISIBLE);
	}

	@SuppressWarnings("SameReturnValue")
	private boolean handleMenuItemClick(MenuItem menuItem) {
		/*
			The dashboard activity needs to run in its own task that does not show up in the recents screen so it feels like a dialog rather than a full activity.
			However, the activities we launch from the dashboard should have normal activity behaviour, so we launch them into a separate task.
		 */
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
		
		switch (menuItem.getItemId()) {
			case R.id.action_help: {
				Intent intent = new Intent(DashboardActivity.this, InstructionsActivity.class).addFlags(flags);
				startActivity(intent);
				break;
			}
			case R.id.action_incompatible_apps: {
				Intent intent = new Intent(DashboardActivity.this, IncompatibleAppsActivity.class).addFlags(flags);
				startActivity(intent);
				break;
			}
			case R.id.action_uninstall:
				Uninstaller.uninstall(DashboardActivity.this);
				break;
			default:
				throw new RuntimeException("Unknown navigation menu item: " + menuItem.getItemId());
		}
		
		return true;
	}

}
