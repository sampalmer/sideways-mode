package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.Settings.Listener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {
	
    private Settings settings;
	private ToggleButton button;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        settings = new Settings(this);
        button = (ToggleButton)findViewById(R.id.onOffButton);
        
        button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.setEnabled(isChecked);
				updateServiceState(isChecked);
			}

		});
        
        settings.listen(new Listener() {
			@Override
			public void tell() {
				updateFromSettings();
			}
		});
        
        updateFromSettings();
    }

	private void updateFromSettings() {
		boolean enabled = settings.enabled();
		button.setChecked(enabled);
		updateServiceState(enabled);
	}

	private void updateServiceState(boolean enabled) {
		final ServiceController service = new ServiceController(this);
        if (enabled)
        	service.start();
        else
        	service.stop();
	}
    
}
