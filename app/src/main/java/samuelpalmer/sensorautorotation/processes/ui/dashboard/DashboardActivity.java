package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;

public class DashboardActivity extends DialogActivity {
	
	private AlertDialog dialog;

	private DashboardPermissionFragment permissionSection;
	private DashboardDisabledFragment disabledSection;
	private ProgressBar progressBar;
	private DashboardEnabledFragment enabledSection;
	
	private ApplicationStatusClient serviceClient;
	
	private ApplicationStatus lastStatus;
	private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = new AlertDialog.Builder(this)
			.setView(R.layout.dashboard)
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
				startActivity(new Intent(DashboardActivity.this, MainSettingsActivity.class));
				finish();
			}
		});
		
		fragmentManager = getSupportFragmentManager();
		
		permissionSection = (DashboardPermissionFragment) fragmentManager.findFragmentById(R.id.dashboard_permission);
		disabledSection = (DashboardDisabledFragment) fragmentManager.findFragmentById(R.id.dashboard_disabled_contents);
		progressBar = dialog.findViewById(R.id.dashboard_progress);
		enabledSection = (DashboardEnabledFragment) fragmentManager.findFragmentById(R.id.dashboard_enabled_contents);
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
		
		boolean boundSuccessfully = bindService(
			new Intent(this, ApplicationStatusService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
		
		if (!boundSuccessfully) {
			// The documentation says you should still unbind after a binding failure
			this.unbindService(serviceConnection);
			throw new RuntimeException("Couldn't connect to " + ApplicationStatusService.class.getSimpleName());
		}
		
		showStatus();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (serviceClient != null) {
			try {
				serviceClient.unsubscribe();
			} catch (RemoteException ignored) {
				// The service process died, but that doesn't matter since we're unsubscribing.
			}
			
			serviceClient = null;
		}
		unbindService(serviceConnection);
	}
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceClient = new ApplicationStatusClient(iBinder, statusChanged);
			
			try {
				serviceClient.subscribe();
			} catch (RemoteException e) {
				// Service process died, so we'll have to try again once it's back up.
				serviceClient = null;
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceClient = null;
		}
		
	};
	
	private final ApplicationStatusClient.Listener statusChanged = new ApplicationStatusClient.Listener() {
		
		@Override
		public void onChange(final ApplicationStatus status) {
			lastStatus = status;
			showStatus();
		}
		
	};
	
	@SuppressLint("SetTextI18n")
	private void showStatus() {
		
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		
		if (lastStatus == null) {
			ViewHelpers.ensureVisibility(progressBar, View.VISIBLE);
			transaction
				.hide(enabledSection)
				.hide(disabledSection)
				.hide(permissionSection);
		}
		else {
			ViewHelpers.ensureVisibility(progressBar, View.GONE);
			
			if (!lastStatus.appEnabled) {
				transaction.hide(enabledSection);
				
				boolean hasPermission = Build.VERSION.SDK_INT < 23 || android.provider.Settings.System.canWrite(this);
				if (hasPermission)
					transaction
						.show(disabledSection)
						.hide(permissionSection);
				else
					transaction
						.hide(disabledSection)
						.show(permissionSection);
			}
			else {
				//Don't want the view to change size after loading; it should stay the same
				transaction
					.show(enabledSection)
					.hide(disabledSection)
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
