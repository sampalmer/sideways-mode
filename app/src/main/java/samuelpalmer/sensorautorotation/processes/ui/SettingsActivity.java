package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferences;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesAdaptor;

public class SettingsActivity extends Activity {

	private RemoteSharedPreferencesAdaptor preferences;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			preferences = new RemoteSharedPreferencesAdaptor(RemoteSharedPreferences.Stub.asInterface(service));
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			throw new RuntimeException("Lost service connection");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(Process.context(), RotationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
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
				startActivity(new Intent(this, InstructionsActivity.class));
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
