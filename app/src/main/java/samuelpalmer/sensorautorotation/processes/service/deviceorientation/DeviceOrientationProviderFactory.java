package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;

abstract class DeviceOrientationProviderFactory {

	private static DeviceOrientationProvider[] prioritisedProviders() {
		return new DeviceOrientationProvider[]{
			new SamsungDeviceOrientationProvider(),
			new AospDeviceOrientationProvider()
		};
	}

	public static DeviceOrientationProvider create() {
		for (DeviceOrientationProvider provider : prioritisedProviders())
			if (provider.supported())
				return provider;

		//A user of an HTC One X reported that he/she got this error. Apparently system auto rotation also wasn't working, so it was probably a problem with the phone.
		EnvironmentError.report(R.string.missing_accelerometer_summary, R.string.missing_accelerometer_message, R.string.app_name);
		return new NullDeviceOrientationProvider();
	}

}
