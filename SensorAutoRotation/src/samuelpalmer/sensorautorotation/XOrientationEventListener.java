package samuelpalmer.sensorautorotation;

import android.content.Context;
import com.android.internal.policy.impl.WindowOrientationListener;

public class XOrientationEventListener extends WindowOrientationListener {

	private final X x;

	public XOrientationEventListener(Context context, X x) {
		super(context, null);
		this.x = x;
	}

	@Override
	public void onProposedRotationChanged(int rotation) {
		x.setDeviceOrientation(Orientation.fromAndroidSurfaceRotation(rotation));
	}
	
}
