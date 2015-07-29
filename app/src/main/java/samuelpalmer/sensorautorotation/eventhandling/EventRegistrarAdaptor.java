package samuelpalmer.sensorautorotation.eventhandling;

public abstract class EventRegistrarAdaptor extends IntolerantEventRegistrar {

	@Override
	public void subscribe(EventHandler subscriber) {
		boolean wasEmpty = noSubscriptions();
		super.subscribe(subscriber);
		if (wasEmpty)
			subscribeToAdaptee();
	}

	@Override
	public void unsubscribe(EventHandler subscriber) {
		super.unsubscribe(subscriber);
		if (noSubscriptions())
			unsubscribeFromAdaptee();
	}

	protected abstract void subscribeToAdaptee();
	protected abstract void unsubscribeFromAdaptee();

}
