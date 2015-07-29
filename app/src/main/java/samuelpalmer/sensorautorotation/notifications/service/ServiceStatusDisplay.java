package samuelpalmer.sensorautorotation.notifications.service;

import java.util.Set;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public abstract class ServiceStatusDisplay {

	public abstract void update(NotificationParameters params);
	public abstract Set<EventRegistrar> updateTriggers();
	public abstract void cancel();

}
