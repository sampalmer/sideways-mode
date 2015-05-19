package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final Context context;
	private final ServiceNotification serviceNotification;
	private final OrientationCalculator calculator;

	public ServiceApiBinder(Context context, ServiceNotification serviceNotification, OrientationCalculator calculator) {
		this.context = context;
		this.serviceNotification = serviceNotification;
		this.calculator = calculator;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(context, serviceNotification.mode().summaryResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
