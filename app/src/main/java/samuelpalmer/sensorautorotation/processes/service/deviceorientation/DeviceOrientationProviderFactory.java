package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

abstract class DeviceOrientationProviderFactory {

	public static DeviceOrientationProvider create() {
		DeviceOrientationProvider result;

		result = new SamsungDeviceOrientationProvider();
		if (!result.supported())
			result = new AospDeviceOrientationProvider();

		return result;
	}

}
