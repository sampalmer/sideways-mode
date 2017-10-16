package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;

public class DashboardActivity extends DialogActivity {
	
	private AlertDialog dialog;
	
	private ProgressBar progressBar;
	private ViewGroup normalSectionContainer;
	private DashboardNormalFragment normalSection;
	
	private ApplicationStatusClient statusClient;
	
	private ApplicationStatus lastStatus;
	
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
		
		progressBar = dialog.findViewById(R.id.dashboard_progress);
		normalSection = (DashboardNormalFragment) getSupportFragmentManager().findFragmentById(R.id.dashboard_normal_contents);
		normalSectionContainer = dialog.findViewById(R.id.dashboard_normal_contents_container);
		
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
		// Intentionally preserving space so the dashboard doesn't resize once it finished loading.
		ensureVisibility(progressBar, lastStatus == null ? View.VISIBLE : View.INVISIBLE);
		ensureVisibility(normalSectionContainer, lastStatus == null ? View.INVISIBLE : View.VISIBLE);
		
		if (lastStatus != null)
			normalSection.setStatus(lastStatus);
	}
	
	/**
	 * Only changes the visibility if it's different to the current value.
	 * This is to minimise lag.
	 */
	private static void ensureVisibility(View control, int newVisibility) {
		int currentVisibility = control.getVisibility();
		if (currentVisibility != newVisibility)
			control.setVisibility(newVisibility);
	}
	
}
