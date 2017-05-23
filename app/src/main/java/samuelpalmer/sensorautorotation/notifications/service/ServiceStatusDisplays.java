package samuelpalmer.sensorautorotation.notifications.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;

public class ServiceStatusDisplays implements ServiceStatusDisplay {

	private final Set<ServiceStatusDisplay> displays = new HashSet<>();
	
	private ServiceStatus lastServiceStatus;
	
	public ServiceStatusDisplays(RotationService service) {
		add(new NotificationStatusDisplay(service));
		add(new ServiceStatusContentProvider.RemoteStatusDisplay());
	}
	
	@SuppressWarnings("WeakerAccess")
	public void add(ServiceStatusDisplay display) {
		displays.add(display);

		if (lastServiceStatus != null)
			display.update(lastServiceStatus);
	}

	@SuppressWarnings("unused")
	public void remove(ServiceStatusDisplay display) {
		displays.remove(display);
	}

	@Override
	public void onServiceStopped() {
		for (ServiceStatusDisplay display : displays)
			display.onServiceStopped();
	}

	@Override
	public void update(ServiceStatus serviceStatus) {
		for (ServiceStatusDisplay display : displays)
			display.update(serviceStatus);

		lastServiceStatus = serviceStatus;
	}

}
