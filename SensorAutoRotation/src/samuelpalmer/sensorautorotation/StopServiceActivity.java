package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.os.Bundle;

public class StopServiceActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ServiceController(this).stop();
        finish();
    }
	
}
