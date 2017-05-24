package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

//Intentionally not inheriting from custom activity base classes since we don't want any extra behaviour for this activity since it's an error message intended to guide the user away from using the app.
public class DeviceUnsupportedActivity extends AppCompatActivity {
	
	/**
	 * String resource id for explanation text.
	 */
	public static final String EXTRA_EXPLANATION = "explanation";
	
	private static final String TAG_UNINSTALLER = "uninstaller";
	
	private UninstallerFragment uninstaller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_unsupported);
		
		int explanationStringResource = getIntent().getIntExtra(EXTRA_EXPLANATION, 0);
		if (explanationStringResource == 0)
			throw new RuntimeException("Explanation not provided");
		TextView explanationView = (TextView) findViewById(R.id.device_unsupported_explanation);
		explanationView.setText(explanationStringResource);
		
		Button uninstallButton = (Button) findViewById(R.id.device_unsupported_uninstall);
		uninstallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				uninstaller.uninstall();
			}
		});
		
		uninstaller = (UninstallerFragment) getSupportFragmentManager().findFragmentByTag(TAG_UNINSTALLER);
		if (uninstaller == null) {
			uninstaller = new UninstallerFragment();
			
			getSupportFragmentManager()
				.beginTransaction()
				.add(uninstaller, TAG_UNINSTALLER)
				.commit();
		}
	}
	
}
