package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.os.Bundle;

abstract class UiActivity extends Activity {
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UiProcess.ensureInitialised();
		onCreate();
	}

	protected abstract void onCreate();
}
