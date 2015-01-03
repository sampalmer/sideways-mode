package samuelpalmer.sensorautorotation.components;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;

public class MainActivity extends ActionBarActivity {

	private Settings settings;
	private ToggleButton button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = new Settings(this);
		settings.listen(new Settings.Listener() {
			@Override
			public void tell() {
				updateFromSettings();
			}
		});

		button = (ToggleButton) findViewById(R.id.onOffButton);
		button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.enabled().set(isChecked);
			}
		});

		updateFromSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		settings.stopListening();
		super.onDestroy();
	}

	private void updateFromSettings() {
		boolean enabled = settings.enabled().getOr(true);
		button.setChecked(enabled);
		updateServiceState(enabled);
	}

	private void updateServiceState(boolean enabled) {
		final ServiceController service = new ServiceController(this);
		if (enabled)
			service.ensureStarted();
		else
			service.ensureStopped();
	}

}
