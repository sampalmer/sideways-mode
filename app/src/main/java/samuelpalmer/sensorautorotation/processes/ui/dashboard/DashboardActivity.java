package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;

public class DashboardActivity extends DialogActivity {
	
	private AlertDialog dialog;

	private DashboardPermissionFragment permissionSection;
	private ProgressBar progressBar;
	private DashboardNormalFragment enabledSection;
	
	private ApplicationStatusClient statusClient;
	
	private ApplicationStatus lastStatus;
	private FragmentManager fragmentManager;
	
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
		
		fragmentManager = getSupportFragmentManager();
		
		permissionSection = (DashboardPermissionFragment) fragmentManager.findFragmentById(R.id.dashboard_permission);
		progressBar = dialog.findViewById(R.id.dashboard_progress);
		enabledSection = (DashboardNormalFragment) fragmentManager.findFragmentById(R.id.dashboard_normal_contents);
		
		statusClient = new ApplicationStatusClient(this, statusChanged);
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
		statusClient.subscribe();
		showStatus();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		statusClient.unsubscribe();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showStatus(); // Just in case the permission has changed
	}
	
	private final ApplicationStatusClient.Listener statusChanged = new ApplicationStatusClient.Listener() {
		
		@Override
		public void onChange(final ApplicationStatus status) {
			if (status != null) {
				lastStatus = status;
				showStatus();
			}
		}
		
	};
	
	@SuppressLint("SetTextI18n")
	private void showStatus() {
		
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		
		if (lastStatus == null) {
			ViewHelpers.ensureVisibility(progressBar, View.VISIBLE);
			transaction
				.hide(enabledSection)
				.hide(permissionSection);
		}
		else {
			ViewHelpers.ensureVisibility(progressBar, View.GONE);
			
			boolean hasPermission = Build.VERSION.SDK_INT < 23 || android.provider.Settings.System.canWrite(this);
			
			if (!hasPermission) {
				transaction
					.hide(enabledSection)
					.show(permissionSection);
			}
			else {
				//Don't want the view to change size after loading; it should stay the same
				transaction
					.show(enabledSection)
					.hide(permissionSection);
				
				enabledSection.setStatus(lastStatus);
			}
		}
		
		/*
		 * I'm allowing state loss here to work around the following exception:
		 * java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
		 *
		 * This is acceptable because the UI will be updated the next time onStart() is called,
		 * so state loss shouldn't cause any noticeable problems.
		 */
		transaction.commitAllowingStateLoss();
	}
	
}
