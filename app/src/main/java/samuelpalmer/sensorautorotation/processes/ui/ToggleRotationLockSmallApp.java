package samuelpalmer.sensorautorotation.processes.ui;

import com.sony.smallapp.SmallApplication;

import samuelpalmer.sensorautorotation.processes.service.RotationLockToggler;

public class ToggleRotationLockSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		RotationLockToggler.toggle();
		finish();
	}

}
