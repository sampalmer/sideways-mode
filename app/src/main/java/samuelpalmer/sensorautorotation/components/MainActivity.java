package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;
import samuelpalmer.sensorautorotation.VersionInfo;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Settings.Setting<Integer> instructedVersion = new Settings(this).lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			instructedVersion.set(new VersionInfo(this).installedVersionCode());

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
				Uri packageUri = Uri.parse("package:" + getPackageName());
				Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
				new Settings(this).enabled().set(false);
				new ServiceController(this).ensureStopped();
				startActivity(uninstallIntent);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

}
