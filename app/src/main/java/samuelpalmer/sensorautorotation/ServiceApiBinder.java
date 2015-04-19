package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;
import samuelpalmer.sensorautorotation.orientationenforcement.AppPackage;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementMonitor;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final Context context;
	private final ServiceNotification serviceNotification;
	private final OrientationCalculator calculator;
	private final OrientationEnforcementMonitor monitor;

	public ServiceApiBinder(Context context, ServiceNotification serviceNotification, OrientationCalculator calculator, OrientationEnforcementMonitor monitor) {
		this.context = context;
		this.serviceNotification = serviceNotification;
		this.calculator = calculator;
		this.monitor = monitor;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(context, serviceNotification.mode().summaryResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	public void reportAppChange(AppPackage current) {
		monitor.updateEnforcement(current);
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}

}
