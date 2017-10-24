package samuelpalmer.sensorautorotation.processes.service;

import android.app.Activity;
import android.os.Bundle;

/**
 * Intentionally does nothing.
 */
public class DummyActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		finish();
	}
}
