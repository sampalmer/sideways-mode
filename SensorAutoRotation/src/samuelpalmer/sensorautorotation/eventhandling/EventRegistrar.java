package samuelpalmer.sensorautorotation.eventhandling;

public interface EventRegistrar<T> {

	void ensureSubscribed(EventHandler<T> subscriber);

	void unsubscribe(EventHandler<T> subscriber);

}