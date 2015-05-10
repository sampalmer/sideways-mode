package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final Context context;
	private final ServiceNotification serviceNotification;
	private final OrientationCalculator calculator;
	private final ActivityMonitor activityMonitor;

	public ServiceApiBinder(Context context, ServiceNotification serviceNotification, OrientationCalculator calculator, ActivityMonitor activityMonitor) {
		this.context = context;
		this.serviceNotification = serviceNotification;
		this.calculator = calculator;
		this.activityMonitor = activityMonitor;
	}

	public void toggleRotation() {
		setAutoRotate(!calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(context, serviceNotification.mode().summaryResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	public void updateCurrentApp(String currentApp) {
		activityMonitor.setCurrentApp(currentApp);
	}

	private void setAutoRotate(boolean newValue) {
		calculator.setAutoRotate(newValue);
	}
}
