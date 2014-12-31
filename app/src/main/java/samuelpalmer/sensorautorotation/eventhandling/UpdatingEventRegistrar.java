package samuelpalmer.sensorautorotation.eventhandling;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.EnvironmentErrorNotifier;

public class UpdatingEventRegistrar<T> implements EventRegistrar<T> {

	private final Set<EventHandler<T>> subscriptions = new HashSet<>();
	private final EnvironmentErrorNotifier errorHandler;

	public UpdatingEventRegistrar(EnvironmentErrorNotifier errorHandler) {
		this.errorHandler = errorHandler;
	}

	@Override
	public void subscribe(EventHandler<T> subscriber) {
		if (subscribed(subscriber))
			throw new RuntimeException("Already subscribed: " + subscriber);

		subscriptions.add(subscriber);
	}

	@Override
	public void unsubscribe(EventHandler<T> subscriber) {
		if (!subscribed(subscriber))
			throw new RuntimeException("Unknown subscriber: " + subscriber);

		subscriptions.remove(subscriber);
	}

	private boolean subscribed(EventHandler<T> subscriber) {
		return subscriptions.contains(subscriber);
	}

	public void report(T event) {
		try {
			for (EventHandler<T> subscriber : subscriptions)
				subscriber.update(event);
		} catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
	}

}
