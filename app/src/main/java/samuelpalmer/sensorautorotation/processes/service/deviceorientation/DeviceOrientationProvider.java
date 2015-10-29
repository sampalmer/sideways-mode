package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

interface DeviceOrientationProvider {
	boolean supported();
	void setListener(DeviceOrientationListener listener);
	void enable();
	void disable();
	void setBias(int rotation);
}
