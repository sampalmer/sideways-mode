package samuelpalmer.sensorautorotation.eventhandling;

import java.util.HashSet;
import java.util.Set;

public abstract class IntolerantEventRegistrar {
	private final Set<EventHandler> subscriptions = new HashSet<>();
	
	public void subscribe(EventHandler subscriber) {
		if (subscribed(subscriber))
			throw new RuntimeException("Already subscribed: " + subscriber);

		subscriptions.add(subscriber);
	}
	
	public void unsubscribe(EventHandler subscriber) {
		if (!subscribed(subscriber))
			throw new RuntimeException("Unknown subscriber: " + subscriber);

		subscriptions.remove(subscriber);
	}

	private boolean subscribed(EventHandler subscriber) {
		return subscriptions.contains(subscriber);
	}

	protected void report() {
		// If a subscriber adds or removes a subscription, we'll get a ConcurrentModificationException.
		// Making a clone of the subscriptions should prevent that.
		HashSet<EventHandler> subscriptionsClone = new HashSet<>(subscriptions);
		
		for (EventHandler subscriber : subscriptionsClone)
			subscriber.update();
	}

	protected boolean noSubscriptions() {
		return subscriptions.isEmpty();
	}
}
