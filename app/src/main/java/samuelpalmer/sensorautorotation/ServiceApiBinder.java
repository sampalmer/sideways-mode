package samuelpalmer.sensorautorotation;

import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationGenerator;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final ServiceNotificationGenerator serviceNotificationGenerator;
	private final OrientationCalculator calculator;

	public ServiceApiBinder(ServiceNotificationGenerator serviceNotificationGenerator, OrientationCalculator calculator) {
		this.serviceNotificationGenerator = serviceNotificationGenerator;
		this.calculator = calculator;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(ThisApplication.context(), serviceNotificationGenerator.mode().autoRotationStateResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
