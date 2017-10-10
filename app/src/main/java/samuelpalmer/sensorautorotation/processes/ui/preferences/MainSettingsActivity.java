package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.InstructionsActivity;
import samuelpalmer.sensorautorotation.processes.ui.UninstallerFragment;
import samuelpalmer.sensorautorotation.processes.ui.feedback.SendFeedbackActivity;

public class MainSettingsActivity extends PreferencesActivity {
	
	private static final String TAG_UNINSTALLER = "uninstaller";
	
	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private UninstallerFragment uninstaller;
	
	private final NavigationView.OnNavigationItemSelectedListener itemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
		@SuppressWarnings("SameReturnValue")
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			drawerLayout.closeDrawers();

			switch (item.getItemId()) {
				case R.id.action_home:
					break;
				case R.id.action_help: {
					Intent intent = new Intent(CurrentProcess.context(), InstructionsActivity.class);
					startActivity(intent);
					break;
				}
				case R.id.action_feedback: {
					Intent intent = new Intent(CurrentProcess.context(), SendFeedbackActivity.class);
					startActivity(intent);
					break;
				}
				case R.id.action_uninstall:
					uninstaller.uninstall();
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
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.action_open_drawer, R.string.action_close_drawer);

		navigation.setNavigationItemSelectedListener(itemSelectedListener);

		// Set the drawer toggle as the DrawerListener
		drawerLayout.addDrawerListener(actionBarDrawerToggle);

		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		supportActionBar.setHomeButtonEnabled(true);
		
		navigation.setCheckedItem(R.id.action_home);
		
		uninstaller = (UninstallerFragment) getSupportFragmentManager().findFragmentByTag(TAG_UNINSTALLER);
		
		if (uninstaller == null) {
			uninstaller = new UninstallerFragment();
			getSupportFragmentManager()
				.beginTransaction()
				.add(uninstaller, TAG_UNINSTALLER)
				.commit();
		}
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

}
