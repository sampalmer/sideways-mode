package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsScreenFragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsFragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;

public class MainActivity extends PreferencesActivity {

	private static final String TAG_FIRST_RUN_FRAGMENT = "first run";

	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private ActionBarDrawerToggle actionBarDrawerToggle;

	private final NavigationView.OnNavigationItemSelectedListener itemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			drawerLayout.closeDrawers();
			return selectItem(item.getItemId());
		}
	};

	private boolean selectItem(int menuItemId) {
		Fragment newFragment = null;

		switch (menuItemId) {
			case R.id.action_settings:
				newFragment = new MainSettingsFragment();
				break;
			case R.id.action_help:
				newFragment = new InstructionsScreenFragment();
				break;
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
				throw new RuntimeException("Unknown navigation menu item: " + menuItemId);
		}

		if (newFragment != null) {
			getFragmentManager().beginTransaction()
						.replace(R.id.coordinator_layout, newFragment)
						.commit();

			return true;
		}
		else
			return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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

		//Other stuff
		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		if (getFragmentManager().findFragmentByTag(TAG_FIRST_RUN_FRAGMENT) == null)
			transaction.add(new FirstRunLauncherFragment(), TAG_FIRST_RUN_FRAGMENT);

		transaction.commit();

		if (savedInstanceState == null)
			if (selectItem(R.id.action_settings))
				navigation.setCheckedItem(R.id.action_settings);
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
