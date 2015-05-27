package samuelpalmer.sensorautorotation.eventhandling;

public interface EventRegistrar {

	void subscribe(EventHandler subscriber);

	void unsubscribe(EventHandler subscriber);

}