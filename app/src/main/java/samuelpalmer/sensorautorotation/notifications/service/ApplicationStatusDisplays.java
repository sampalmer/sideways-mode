package samuelpalmer.sensorautorotation.notifications.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.ServiceRegistrar;

public class ApplicationStatusDisplays extends State implements ApplicationStatusDisplay {

	private final Set<ApplicationStatusDisplay> displays = new HashSet<>();
	private final Set<NotificationStatusDisplay> serviceDisplays = new HashSet<>();
	private ApplicationStatus lastApplicationStatus;
	
	public ApplicationStatusDisplays() {
		for (RotationService service : ServiceRegistrar.instance.getServiceInstances())
			addServiceDisplay(service);
		
		add(new ApplicationStatusService.RemoteStatusDisplay());
	}
	
	@Override
	protected void start() {
		ServiceRegistrar.instance.subscribe(servicesChanged);
	}
	
	@Override
	protected void stop() {
		ServiceRegistrar.instance.unsubscribe(servicesChanged);
	}
	
	private final EventHandler servicesChanged = new EventHandler() {
		@Override
		public void update() {
			for (NotificationStatusDisplay serviceDisplay : serviceDisplays)
				remove(serviceDisplay);
			serviceDisplays.clear();
			
			for (RotationService service : ServiceRegistrar.instance.getServiceInstances())
				addServiceDisplay(service);
		}
	};
	
	private void addServiceDisplay(RotationService service) {
		NotificationStatusDisplay serviceDisplay = new NotificationStatusDisplay(service);
		add(serviceDisplay);
		serviceDisplays.add(serviceDisplay);
	}
	
	@SuppressWarnings("WeakerAccess")
	public void add(ApplicationStatusDisplay display) {
		displays.add(display);

		if (lastApplicationStatus != null)
			display.update(lastApplicationStatus);
	}

	@SuppressWarnings("unused")
	public void remove(ApplicationStatusDisplay display) {
		displays.remove(display);
	}

	@Override
	public void onApplicationStopped() {
		for (ApplicationStatusDisplay display : displays)
			display.onApplicationStopped();
	}

	@Override
	public void update(ApplicationStatus applicationStatus) {
		for (ApplicationStatusDisplay display : displays)
			display.update(applicationStatus);

		lastApplicationStatus = applicationStatus;
	}
	
}
