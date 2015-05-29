package samuelpalmer.sensorautorotation.components;

import com.sony.smallapp.SmallApplication;

import samuelpalmer.sensorautorotation.RotationLockToggler;

public class ToggleRotationLockSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		new RotationLockToggler().go();
		finish();
	}

}
