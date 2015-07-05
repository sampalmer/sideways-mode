package samuelpalmer.sensorautorotation.processes.service;

import com.sony.smallapp.SmallApplication;

public class ToggleRotationLockSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		RotationLockToggler.updateAndToggle();
		finish();
	}

}
