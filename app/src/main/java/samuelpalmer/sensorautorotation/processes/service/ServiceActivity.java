package samuelpalmer.sensorautorotation.processes.service;

import android.app.Activity;
import android.os.Bundle;

abstract class ServiceActivity extends Activity {
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServiceProcess.ensureInitialised();
		onCreate();
	}

	protected abstract void onCreate();
}
