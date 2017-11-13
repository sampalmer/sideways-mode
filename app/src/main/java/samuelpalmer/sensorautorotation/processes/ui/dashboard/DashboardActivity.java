package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.InstructionsActivity;
import samuelpalmer.sensorautorotation.processes.ui.Uninstaller;
import samuelpalmer.sensorautorotation.processes.ui.feedback.ReportProblemActivity;
import samuelpalmer.sensorautorotation.utilities.IntentExtensions;
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
		
		ImageButton menuButton = dialog.findViewById(R.id.dashboard_menu);
		assert menuButton != null;
		menuButton.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("RestrictedApi")
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(DashboardActivity.this, v);
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						
						switch (item.getItemId()) {
							case R.id.action_help: {
								Intent intent = new Intent(DashboardActivity.this, InstructionsActivity.class);
								startActivity(intent);
								break;
							}
							case R.id.action_review: {
								Intent intent = appMarketIntent();
								
								try {
									startActivity(intent);
								} catch (ActivityNotFoundException ex) {
									Toast.makeText(getApplicationContext(), R.string.error_no_market_app, Toast.LENGTH_LONG).show();
								}
								
								break;
							}
							case R.id.action_report: {
								Intent intent = new Intent(DashboardActivity.this, ReportProblemActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
								startActivity(intent);
								
								break;
							}
							case R.id.action_uninstall:
								Uninstaller.uninstall(DashboardActivity.this);
								break;
							default:
								throw new RuntimeException("Unknown navigation menu item: " + item.getItemId());
						}
						
						return true;
					}
				});
				popup.inflate(R.menu.main);
				
				Menu menu = popup.getMenu();
				
				MenuItem reviewMenuItem = menu.findItem(R.id.action_review);
				if (reviewMenuItem == null)
					throw new RuntimeException("Couldn't find Review menu item.");
				
				if (IntentExtensions.activityDoesntExist(appMarketIntent(), getPackageManager()))
					menu.removeItem(reviewMenuItem.getItemId());
				
				popup.show();
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
	
	private Intent appMarketIntent() {
		return new Intent(Intent.ACTION_VIEW)
		.setData(Uri.parse("market://details?id=" + DashboardActivity.this.getPackageName()));
	}
	
}
