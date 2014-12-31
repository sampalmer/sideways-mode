package samuelpalmer.sensorautorotation.eventhandling;

public interface EventHandler<T> {
	void update(T event);
}
