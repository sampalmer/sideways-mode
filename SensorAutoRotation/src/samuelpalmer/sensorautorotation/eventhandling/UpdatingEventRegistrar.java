package samuelpalmer.sensorautorotation.eventhandling;

import java.util.HashSet;
import java.util.Set;

public class UpdatingEventRegistrar<T> implements EventRegistrar<T> {
	
	private final Set<EventHandler<T>> subscriptions = new HashSet<EventHandler<T>>();
	
	@Override
	public void ensureSubscribed(EventHandler<T> subscriber) {
		subscriptions.add(subscriber);
	}
	
	@Override
	public void unsubscribe(EventHandler<T> subscriber) {
		if (!subscriptions.contains(subscriber))
			throw new RuntimeException("Unknown subscriber: " + subscriber);
		
		subscriptions.remove(subscriber);
	}
	
	public void report(T event) {
		for (EventHandler<T> subscriber : subscriptions)
			subscriber.update(event);
	}
	
}
