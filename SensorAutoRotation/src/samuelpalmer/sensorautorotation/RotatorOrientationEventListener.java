package samuelpalmer.sensorautorotation;

import android.content.Context;
import com.android.internal.policy.impl.WindowOrientationListener;

public class RotatorOrientationEventListener extends WindowOrientationListener {

	private final AutoRotator rotator;

	public RotatorOrientationEventListener(Context context, AutoRotator rotator) {
		super(context, null);
		this.rotator = rotator;
	}

	@Override
	public void onProposedRotationChanged(int rotation) {
		Orientation orientation = Orientation.fromAndroidSurfaceRotation(rotation);
		rotator.setDeviceOrientation(orientation);
	}
	
}
