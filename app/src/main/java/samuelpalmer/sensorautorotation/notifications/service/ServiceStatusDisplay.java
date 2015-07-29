package samuelpalmer.sensorautorotation.notifications.service;

import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public abstract class ServiceStatusDisplay extends State {

	public abstract void update(NotificationParameters params);
	public abstract Set<EventRegistrar> updateTriggers();

}
