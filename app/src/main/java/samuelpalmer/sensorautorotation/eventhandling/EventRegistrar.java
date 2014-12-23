package samuelpalmer.sensorautorotation.eventhandling;

public interface EventRegistrar<T> {

	void subscribe(EventHandler<T> subscriber);

	void unsubscribe(EventHandler<T> subscriber);

}