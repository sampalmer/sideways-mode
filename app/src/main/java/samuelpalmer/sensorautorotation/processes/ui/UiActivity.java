package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.processes.service.EnsureAppLoadedReceiver;

/**
 * Ensures the background service is running if necessary in case it's died.
 */
public abstract class UiActivity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		ensureBackgroundProcessIsLoaded();
	}

	private void ensureBackgroundProcessIsLoaded() {
		Intent intent = ComponentType.create(EnsureAppLoadedReceiver.class).intent();
		sendBroadcast(intent);
	}

}
