package samuelpalmer.sensorautorotation.components;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.settings.application.SettingsFragment;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new ServiceController().updateFromSettings();

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
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
