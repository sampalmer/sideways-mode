package samuelpalmer.sensorautorotation.eventhandling;

import samuelpalmer.sensorautorotation.EnvironmentException;

public interface EventHandler<T> {
	void update(T event) throws EnvironmentException;
}
