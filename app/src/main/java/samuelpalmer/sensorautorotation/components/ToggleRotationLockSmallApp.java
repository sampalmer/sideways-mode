package samuelpalmer.sensorautorotation.components;

import com.sony.smallapp.SmallApplication;

import samuelpalmer.sensorautorotation.RotationLockToggler;

public class ToggleRotationLockSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		new RotationLockToggler(this, true, callback).go();
	}

	private final RotationLockToggler.CompletionCallback callback = new RotationLockToggler.CompletionCallback() {
		@Override
		public void completed() {
			finish();
		}
	};

}
