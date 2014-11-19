package samuelpalmer.sensorautorotation.components;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;

import samuelpalmer.sensorautorotation.R;
import android.content.Intent;
import android.widget.ToggleButton;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    MainActivity activity;
	private ShadowActivity shadowActivity;
	private ToggleButton button;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        shadowActivity = Robolectric.shadowOf(activity);
        button = (ToggleButton)activity.findViewById(R.id.onOffButton);
    }
    
    @Test
    public void buttonShouldBeOn() {
    	assertThat(button.isChecked(), equalTo(true));
    }
    
    @Test
    public void shouldStartService() {
    	Intent startIntent = shadowActivity.getNextStartedService();
    	isForService(startIntent);
    }
    
    @Test
    public void pressingButtonShouldStopService() {
    	button.toggle();
    	
    	Intent stopIntent = shadowActivity.getNextStoppedService();
    	isForService(stopIntent);
    }

	private void isForService(Intent intent) {
		assertThat(intent.getComponent().getClassName(), equalTo(RotationService.class.getCanonicalName()));
	}
}