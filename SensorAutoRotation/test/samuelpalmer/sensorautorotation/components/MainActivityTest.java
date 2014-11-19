package samuelpalmer.sensorautorotation.components;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowPreferenceManager;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.Settings;
import android.content.Intent;
import android.widget.ToggleButton;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    MainActivity activity;
	private ShadowActivity shadowActivity;
	private ToggleButton button;
	private Settings settings;
	
	@Before
	public void setup() {
		settings = new Settings(Robolectric.application);
	}
	
    @Test
    public void buttonShouldBeOn() {
    	startActivity();
    	
    	assertThat(button.isChecked(), equalTo(true));
    }
    
    @Test
    public void shouldStartService() {
    	startActivity();
    	
    	Intent startIntent = shadowActivity.getNextStartedService();
    	isForService(startIntent);
    }
    
    @Test
    public void shouldNotStartServiceWhenDisabled() {
    	settings.setEnabled(false);
    	
    	startActivity();
    	
    	Intent startIntent = shadowActivity.getNextStartedService();
    	assertThat(startIntent, equalTo(null));
    }
    
    @Test
    public void pressingButtonShouldStopService() {
    	startActivity();
    	
    	button.toggle();
    	
    	Intent stopIntent = shadowActivity.getNextStoppedService();
    	isForService(stopIntent);
    }
    
    private void startActivity() {
		activity = Robolectric.buildActivity(MainActivity.class).create().get();
        shadowActivity = Robolectric.shadowOf(activity);
        button = (ToggleButton)activity.findViewById(R.id.onOffButton);
	}
	
	private void isForService(Intent intent) {
		assertThat(intent.getComponent().getClassName(), equalTo(RotationService.class.getCanonicalName()));
	}
}