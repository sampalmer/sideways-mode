package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.SettingsService;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferences;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesAdaptor;

/**
 * UI proxy to settings stored in background process.
 */
public class SettingsActivity extends ServiceSyncActivity {

	private RemoteSharedPreferencesAdaptor preferences;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			preferences = new RemoteSharedPreferencesAdaptor(RemoteSharedPreferences.Stub.asInterface(service));

			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//We could have got here as a result of the user explicitly stopping the background service, so we shouldn't crash.
			//If the service crashed, there's not much benefit to the UI also crashing. Might as well gracefully exit.
			//Once the user re-opens the task, this activity should be created again anyway.
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Prevents the fragment from automatically being recreated.
		// We need to do this since the fragment shouldn't exist without the service binding.
		super.onCreate(null);

		bindService(new Intents(SettingsService.class).makeIntent(), serviceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	@Override
	protected void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle outState) {
		//Don't invoke super.onSaveInstanceState since it'll prevent us from adding/removing the fragment while we're paused
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		return name.equals(ApplicationSettings.sharedPreferencesName()) ? preferences : super.getSharedPreferences(name, mode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_instructions:
				startActivity(new Intents(InstructionsActivity.class).makeIntent());
				break;
			case R.id.action_feedback:
				new Feedback().send();
				break;
			case R.id.action_uninstall:
				confirmUninstall();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void confirmUninstall() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.uninstall_title)
				.setIcon(android.R.drawable.ic_menu_delete)
				.setMessage(R.string.uninstall_confirmation_message)
				.setPositiveButton(android.R.string.yes, new Uninstaller(this))
				.setNegativeButton(android.R.string.no, null)
				.create()
				.show();
	}

}
