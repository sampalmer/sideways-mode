package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.EnvironmentException;

public class NotificationPressedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			RotationService.overlay.ensureStarted();
		} catch (EnvironmentException e) {
			RotationService.errorHandler.handle(e);
		}

		finish();
	}
}
