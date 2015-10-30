package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

class NullDeviceOrientationProvider implements DeviceOrientationProvider {
	@Override
	public boolean supported() {
		return true;
	}

	@Override
	public void setListener(DeviceOrientationListener listener) {

	}

	@Override
	public void enable() {

	}

	@Override
	public void disable() {

	}

	@Override
	public void setBias(int rotation) {

	}
}
