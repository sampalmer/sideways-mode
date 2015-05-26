package samuelpalmer.sensorautorotation;

import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationUpdater;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final ServiceNotificationUpdater serviceNotificationUpdater;
	private final OrientationCalculator calculator;

	public ServiceApiBinder(ServiceNotificationUpdater serviceNotificationUpdater, OrientationCalculator calculator) {
		this.serviceNotificationUpdater = serviceNotificationUpdater;
		this.calculator = calculator;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(ThisApplication.context(), serviceNotificationUpdater.mode().autoRotationStateResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
