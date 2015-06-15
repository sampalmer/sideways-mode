package samuelpalmer.sensorautorotation.processes.service;

public class ToggleRotationLockActivity extends ServiceActivity {

	@Override
	protected void onCreate() {
		RotationLockToggler.toggle();
		finish();
	}

}
