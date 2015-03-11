package samuelpalmer.sensorautorotation.notification.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;

class LockedMode implements Mode {

	private final Context context;
	private final OrientationCalculator calculator;

	public LockedMode(Context context, OrientationCalculator calculator) {
		this.context = context;
		this.calculator = calculator;
	}

	@Override
	public int iconResourceId() {
		return isPortrait() ? R.drawable.ic_screen_rotation_locked_portrait : R.drawable.ic_screen_rotation_locked_landscape;
	}

	public boolean isPortrait() {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		return inNaturalOrientation == deviceIsNaturallyPortrait();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private boolean deviceIsNaturallyPortrait() {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point screenResolution = new Point();

		//Getting the screen dimensions and rotation should be as close together as possible
		//since it's possible for them to be retrieved out of sync
		if (Build.VERSION.SDK_INT >= 14)
			display.getRealSize(screenResolution);
		else
			display.getSize(screenResolution);
		int rotation = display.getRotation();

		Orientation orientation = new AndroidSurfaceRotation(rotation).orientation();

		boolean portrait = screenResolution.x < screenResolution.y;
		return portrait == orientation.isNaturalOrUpsideDown();
	}

	@Override
	public int summaryResourceId() {
		return R.string.screen_rotation_disabled;
	}

	@Override
	public int actionDescriptionResourceId() {
		return R.string.enable_screen_rotation;
	}
}
