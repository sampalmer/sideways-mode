package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new ServiceController(this).updateFromSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
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
				.setTitle("Uninstall?")
				.setIcon(android.R.drawable.ic_menu_delete)
				.setMessage("Rotation control by this app will be disabled. (You can re-enable it in this app's settings screen.) Disable and uninstall this app?")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						uninstall();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {}
				})
				.create()
				.show();
	}

	private void uninstall() {
		Uri packageUri = Uri.parse("package:" + getPackageName());
		Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		new ApplicationSettings(this).enabled().setAndCommit(false);
		new ServiceController(this).ensureStopped();
		startActivity(uninstallIntent);
	}

}
