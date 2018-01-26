package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.InstructionsActivity;
import samuelpalmer.sensorautorotation.processes.ui.Uninstaller;
import samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps.IncompatibleAppsActivity;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;

public class DashboardActivity extends DialogActivity {
	
	private AlertDialog dialog;
	
	private ViewGroup normalSection;
	private ViewGroup permissionSection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = new AlertDialog.Builder(this)
			.setView(R.layout.activity_dashboard)
			.create();
		dialog.setOnDismissListener(dialogInterface -> {
			dialog = null;
			finish();
		});
		dialog.show();
		
		ImageButton menuButton = dialog.findViewById(R.id.dashboard_menu);
		assert menuButton != null;
		menuButton.setOnClickListener(this::showMenu);
		
		normalSection = dialog.findViewById(R.id.dashboard_normal_contents_container);
		permissionSection = dialog.findViewById(R.id.dashboard_permission_contents_container);
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
		ViewUtilities.ensureVisibility(normalSection, hasPermission ? View.VISIBLE : View.GONE);
		ViewUtilities.ensureVisibility(permissionSection, hasPermission ? View.GONE : View.VISIBLE);
	}
	
	private void showMenu(View view) {
		PopupMenu popup = new PopupMenu(DashboardActivity.this, view);
		popup.setOnMenuItemClickListener(this::handleMenuItemClick);
		popup.inflate(R.menu.main);
		popup.show();
	}
	
	private boolean handleMenuItemClick(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.action_help: {
				Intent intent = new Intent(DashboardActivity.this, InstructionsActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.action_incompatible_apps: {
				Intent intent = new Intent(DashboardActivity.this, IncompatibleAppsActivity.class);
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
