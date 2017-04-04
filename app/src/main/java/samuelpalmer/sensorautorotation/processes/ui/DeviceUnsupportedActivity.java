package samuelpalmer.sensorautorotation.processes.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;

//Intentionally not inheriting from custom activity base classes since we don't want any extra behaviour for this activity since it's an error message intended to guide the user away from using the app.
public class DeviceUnsupportedActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_unsupported);

		Button uninstallButton = (Button) findViewById(R.id.device_unsupported_uninstall);
		uninstallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Uninstaller uninstaller = new Uninstaller(DeviceUnsupportedActivity.this);
				uninstaller.uninstallOrPrompt();
			}
		});
	}

}
