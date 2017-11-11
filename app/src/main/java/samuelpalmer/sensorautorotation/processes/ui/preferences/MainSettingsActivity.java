package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.InstructionsActivity;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.processes.ui.Uninstaller;
import samuelpalmer.sensorautorotation.processes.ui.feedback.SendFeedbackActivity;
import samuelpalmer.sensorautorotation.utilities.IntentExtensions;

/**
 * Hosts the navigation drawer and settings.
 */
public class MainSettingsActivity extends UiActivity {
	
	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	
	private final NavigationView.OnNavigationItemSelectedListener itemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@SuppressWarnings("SameReturnValue")
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			drawerLayout.closeDrawers();

			switch (item.getItemId()) {
				case R.id.action_quick_setting: {
					new AlertDialog.Builder(MainSettingsActivity.this)
						.setTitle(R.string.preference_quick_setting)
						.setMessage(R.string.preference_quick_setting_instructions)
						.show();
					break;
				}
				case R.id.action_assist: {
					if (Build.VERSION.SDK_INT >= 23) {
						new AlertDialog.Builder(MainSettingsActivity.this)
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
						new AlertDialog.Builder(MainSettingsActivity.this)
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
					new AlertDialog.Builder(MainSettingsActivity.this)
						.setTitle(R.string.preference_small_app)
						.setMessage(R.string.preference_small_app_instructions)
						.show();
					
					break;
				}
				case R.id.action_help: {
					Intent intent = new Intent(MainSettingsActivity.this, InstructionsActivity.class);
					startActivity(intent);
					break;
				}
				case R.id.action_feedback: {
					Intent intent = new Intent(MainSettingsActivity.this, SendFeedbackActivity.class);
					startActivity(intent);
					break;
				}
				case R.id.action_uninstall:
					Uninstaller.uninstall(MainSettingsActivity.this);
					break;
				default:
					throw new RuntimeException("Unknown navigation menu item: " + item.getItemId());
			}

			return false; //Never want to mark the item as selected since none of the menu items result in a different UI within this activity.
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		//Drawer stuff
		drawerLayout = findViewById(R.id.drawer_layout);
		
		navigation = findViewById(R.id.navigation);
		
		if (Build.VERSION.SDK_INT < 24)
			navigation.getMenu().removeItem(R.id.action_quick_setting);
		
		if (
			Build.VERSION.SDK_INT >= 23 && IntentExtensions.activityDoesntExist(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS), getPackageManager())
			||
			Build.VERSION.SDK_INT >= 16 && hasNavigationBar() == Boolean.FALSE
			||
			Build.VERSION.SDK_INT < 16
		)
			navigation.getMenu().removeItem(R.id.action_assist);
		
		if (!supportsSmallApp())
			navigation.getMenu().removeItem(R.id.action_small_app);
		
//		if (navigation.getMenu().findItem(R.id.menu_group_controls).getSubMenu().size() == 0)
//			navigation.getMenu().removeItem(R.id.menu_group_controls);
//
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.action_open_drawer, R.string.action_close_drawer);

		navigation.setNavigationItemSelectedListener(itemSelectedListener);

		// Set the drawer toggle as the DrawerListener
		drawerLayout.addDrawerListener(actionBarDrawerToggle);

		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		supportActionBar.setHomeButtonEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		actionBarDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		actionBarDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//noinspection SimplifiableIfStatement
		if (actionBarDrawerToggle.onOptionsItemSelected(item))
			return true;

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerVisible(navigation))
			drawerLayout.closeDrawers();
		else
			super.onBackPressed();
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
			Log.w(MainSettingsActivity.class.getSimpleName(), "Couldn't determine whether the device has a navigation bar", e);
			return null;
		}
	}
	
}
