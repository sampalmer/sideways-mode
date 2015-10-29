package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import samuelpalmer.sensorautorotation.Process;

class AospDeviceOrientationProvider implements DeviceOrientationProvider {

	private DeviceOrientationListener listener;

	@Override
	public boolean supported() {
		return windowOrientationListener.canDetectOrientation();
	}

	@Override
	public void setListener(DeviceOrientationListener listener) {
		this.listener = listener;
	}

	@Override
	public void enable() {
		windowOrientationListener.enable();
	}

	@Override
	public void disable() {
		windowOrientationListener.disable();
	}

	@Override
	public void setBias(int rotation) {
		windowOrientationListener.setCurrentRotation(rotation);
	}

	private final WindowOrientationListener windowOrientationListener = new WindowOrientationListener(Process.context(), null) {
		@Override
		public void onProposedRotationChanged(int rotation) {
			listener.changed(rotation);
		}
	};

}
