package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;

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
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				dialog = null;
				finish();
			}
		});
		dialog.show();
		
		ImageButton settingsButton = dialog.findViewById(R.id.dashboard_settings);
		assert settingsButton != null;
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, MainSettingsActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				startActivity(intent);
				finish();
			}
		});
		
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
	
}
