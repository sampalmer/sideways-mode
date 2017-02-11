package samuelpalmer.sensorautorotation.processes.ui;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
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
		switch (menuItemId) {
			case R.id.action_settings:
				getFragmentManager().beginTransaction()
						.replace(R.id.coordinator_layout, new MainSettingsFragment())
						.commit();

				setTitle(R.string.preference_settings_title);

				return true;
			case R.id.action_instructions:
				getFragmentManager().beginTransaction()
						.replace(R.id.coordinator_layout, new InstructionsScreenFragment())
						.commit();

				setTitle(R.string.instructions_heading);

				return true; //TODO: Figure out why it isn't showing as selected inside the drawer
			case R.id.action_feedback_email:
				new Email(
						MainActivity.this,
						ReportProblemActivity.emailRecipients(),
						StringResource.format(R.string.email_feedback_subject, R.string.app_name_full, VersionInfo.packageInfo().versionName).toString(),
						""
				).send();

				return false;
			case R.id.action_feedback_google_play: {
				Intent intent = new Intent(Intent.ACTION_VIEW)
						.setData(Uri.parse("market://details?id=" + MainActivity.this.getPackageName()));
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					Toast.makeText(MainActivity.this, R.string.error_no_market_app, Toast.LENGTH_LONG).show();
				}

				return false;
			}
			case R.id.action_report: {
				Intent intent = ComponentType.create(ReportProblemActivity.class)
						.intent()
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
				startActivity(intent);
				return true;
			}
			case R.id.action_uninstall:
				final Uninstaller uninstaller = new Uninstaller(MainActivity.this);
				uninstaller.uninstallOrPrompt();
				return false;
			default:
				throw new RuntimeException("Unknown navigation menu item: " + menuItemId);
		}
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

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		//Other stuff
		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		if (getFragmentManager().findFragmentByTag(TAG_FIRST_RUN_FRAGMENT) == null)
			transaction.add(new FirstRunLauncherFragment(), TAG_FIRST_RUN_FRAGMENT);

		transaction.commit();

		if (savedInstanceState == null)
			selectItem(R.id.action_settings);

		//TODO: The back button should close the navigation drawer
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

}
