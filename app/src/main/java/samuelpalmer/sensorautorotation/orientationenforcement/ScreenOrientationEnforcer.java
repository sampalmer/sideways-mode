package samuelpalmer.sensorautorotation.orientationenforcement;

import android.view.View;

import samuelpalmer.sensorautorotation.State;

public class ScreenOrientationEnforcer extends State {

	private final View view;

	public ScreenOrientationEnforcer(View view) {
		this.view = view;
	}

	@Override
	protected void start() {
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void stop() {
		view.setVisibility(View.INVISIBLE);
	}

}
