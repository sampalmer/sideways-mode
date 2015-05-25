package samuelpalmer.sensorautorotation;

import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final ServiceNotification serviceNotification;
	private final OrientationCalculator calculator;

	public ServiceApiBinder(ServiceNotification serviceNotification, OrientationCalculator calculator) {
		this.serviceNotification = serviceNotification;
		this.calculator = calculator;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(ThisApplication.context(), serviceNotification.calculateMode().autoRotationStateResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
