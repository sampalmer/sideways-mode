package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Binder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;

/**
 * Must be called from process main thread
 */
public class ServiceApiBinder extends Binder {

	private final Context context;
	private final ServiceNotification serviceNotification;

	public ServiceApiBinder(Context context, ServiceNotification serviceNotification) {
		this.context = context;
		this.serviceNotification = serviceNotification;
	}

	public void toggleRotation() {
		setAutoRotate(!RotationService.calculator.autoRotate());
	}

	public void showStatusToast() {
		Toast
			.makeText(context, serviceNotification.mode().summaryResourceId(), Toast.LENGTH_SHORT)
			.show();
	}

	private void setAutoRotate(boolean newValue) {
		RotationService.calculator.setAutoRotate(newValue);
	}

}
