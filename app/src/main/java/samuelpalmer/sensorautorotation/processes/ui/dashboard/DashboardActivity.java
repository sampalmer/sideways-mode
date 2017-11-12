package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
							case R.id.action_quick_setting: {
								new AlertDialog.Builder(DashboardActivity.this)
								.setTitle(R.string.preference_quick_setting)
								.setMessage(R.string.preference_quick_setting_instructions)
								.show();
								break;
							}
							case R.id.action_assist: {
								if (Build.VERSION.SDK_INT >= 23) {
									new AlertDialog.Builder(DashboardActivity.this)
									.setTitle(R.string.preference_assist)
									.setMessage(R.string.preference_assist_instructions_lollipop)
									.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {}
									})
									.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										@TargetApi(Build.VERSION_CODES.LOLLIPOP)
										@Override
										public void onClick(DialogInterface dialog, int which) {
											try {
												startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
											}
											catch (ActivityNotFoundException e) {
												throw new RuntimeException("Couldn't find assist settings activity. The user shouldn't have been able to get this far.");
											}
										}
									})
									.show();
								}
								else {
									new AlertDialog.Builder(DashboardActivity.this)
									.setTitle(R.string.preference_assist)
									.setMessage(R.string.preference_assist_instructions)
									.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {}
									})
									.show();
								}
								
								break;
							}
							case R.id.action_small_app: {
								new AlertDialog.Builder(DashboardActivity.this)
								.setTitle(R.string.preference_small_app)
								.setMessage(R.string.preference_small_app_instructions)
								.show();
								
								break;
							}
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
				if (Build.VERSION.SDK_INT < 24)
					menu.removeItem(R.id.action_quick_setting);
				
				if (
				Build.VERSION.SDK_INT >= 23 && IntentExtensions.activityDoesntExist(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS), getPackageManager())
				||
				Build.VERSION.SDK_INT >= 16 && hasNavigationBar() == Boolean.FALSE
				||
				Build.VERSION.SDK_INT < 16
				)
					menu.removeItem(R.id.action_assist);
				
				if (!supportsSmallApp())
					menu.removeItem(R.id.action_small_app);
				
				MenuItem reviewMenuItem = menu.findItem(R.id.action_review);
				if (reviewMenuItem == null)
					throw new RuntimeException("Couldn't find Review menu item.");
				
				if (IntentExtensions.activityDoesntExist(appMarketIntent(), getPackageManager()))
					menu.removeItem(reviewMenuItem.getItemId());
				
//				if (menu.findItem(R.id.menu_group_controls).getSubMenu().size() == 0)
//					menu.removeItem(R.id.menu_group_controls);
				
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
	
	
	private static boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns {@code null} if this couldn't be determined.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("PrivateApi") // The app is designed to tolerate the private APIs being unavailable
	private static Boolean hasNavigationBar() {
		try {
			Class<?> serviceManager = Class.forName("android.os.ServiceManager");
			IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
			Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
			Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
			Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
			return (boolean)hasNavigationBar.invoke(windowManagerService);
		} catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.w(DashboardActivity.class.getSimpleName(), "Couldn't determine whether the device has a navigation bar", e);
			return null;
		}
	}
	
	private Intent appMarketIntent() {
		return new Intent(Intent.ACTION_VIEW)
		.setData(Uri.parse("market://details?id=" + DashboardActivity.this.getPackageName()));
	}
	
}
