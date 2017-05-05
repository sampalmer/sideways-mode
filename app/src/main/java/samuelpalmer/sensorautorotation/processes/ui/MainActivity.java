package samuelpalmer.sensorautorotation.processes.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;

import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.errors.SensorError;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.ui.feedback.SendFeedbackActivity;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class MainActivity extends UiActivity {

	public static final String EXTRA_TUTORIAL_COMPLETE = "tutorial_complete";
	
	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private EnabledFragment enabledFragment;
	
	private final NavigationView.OnNavigationItemSelectedListener itemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@SuppressWarnings("SameReturnValue")
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			drawerLayout.closeDrawers();

			switch (item.getItemId()) {
				case R.id.action_home:
					break;
				case R.id.action_settings: {
					Intent intent = ComponentType.create(SettingsActivity.class).intent();
					startActivity(intent);
					break;
				}
				case R.id.action_help: {
					Intent intent = ComponentType.create(InstructionsActivity.class).intent();
					startActivity(intent);
					break;
				}
				case R.id.action_feedback: {
					Intent intent = ComponentType.create(SendFeedbackActivity.class).intent();
					startActivity(intent);
					break;
				}
				case R.id.action_uninstall:
					final Uninstaller uninstaller = new Uninstaller(MainActivity.this);
					uninstaller.uninstallOrPrompt();

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
		setContentView(R.layout.activity_main);
		
		//Drawer stuff
		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		navigation = (NavigationView)findViewById(R.id.navigation);
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.action_open_drawer, R.string.action_close_drawer);

		navigation.setNavigationItemSelectedListener(itemSelectedListener);

		// Set the drawer toggle as the DrawerListener
		drawerLayout.addDrawerListener(actionBarDrawerToggle);

		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		supportActionBar.setHomeButtonEnabled(true);
		
		navigation.setCheckedItem(R.id.action_home);
		
		enabledFragment = (EnabledFragment) getFragmentManager().findFragmentById(R.id.fragment_enabled);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		actionBarDrawerToggle.syncState();
	}

	@Override
	protected void onStart() {
		super.onStart();

		//Checking this after the activity starts in case the user wants to go out and back in again to retry the check in case the problem is intermittent
		if (!OrientationSensorHandler.canDetectOrientation()) {
			//Not letting the user go any further since they shouldn't have installed the app
			Log.w(MainActivity.class.getSimpleName(), "Didn't find any supported sensors. Advising user the device is unsupported.");
			try {
				new SensorError(SensorError.Type.NOT_FOUND).showError().send();
			} catch (PendingIntent.CanceledException e) {
				throw new RuntimeException(e);
			}
			
			finish();
			return;
		}
		
		Settings.Setting<Integer> lastVersionUserInstructedAbout = ApplicationSettings.remote().lastVersionUserInstructedAbout();
		if (!lastVersionUserInstructedAbout.hasValue()) {
			boolean tutorialComplete = getIntent().getBooleanExtra(EXTRA_TUTORIAL_COMPLETE, false);
			
			if (!tutorialComplete) {
				Intent intent = ComponentType.create(InstructionsActivity.class)
					.intent()
					.putExtra(InstructionsActivity.EXTRA_FIRST_RUN, true);
				startActivity(intent);
				
				finish();
			}
			else {
				lastVersionUserInstructedAbout.set(VersionInfo.installedVersionCode());
				
				/*
				 * I received a number of error reports from users saying the app didn't work.
				 * When I looked at their logs, it appeared they hadn't actually turned the app on.
				 * (This is probably because they don't speak English.)
				 *
				 * So we'll enable the app automatically.
				 */
				enabledFragment.setEnabled();
			}
		}
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

}
