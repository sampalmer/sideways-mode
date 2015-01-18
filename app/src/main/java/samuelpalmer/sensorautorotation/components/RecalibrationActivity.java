package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

public class RecalibrationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RotationService.calculator.setRecalibrating(true);
	}

}
