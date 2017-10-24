package samuelpalmer.sensorautorotation.utilities.eventhandling;

/**
 * An event that is triggered by listening to something else for events.
 * Basically adapts an external event into an {@link EventRegistrar}.
 */
public abstract class EventRegistrarAdaptor extends EventRegistrar {

	@Override
	public void subscribe(EventHandler subscriber) {
		boolean wasEmpty = noSubscriptions();
		super.subscribe(subscriber);
		if (wasEmpty)
			try {
				subscribeToAdaptee();
			}
			catch (Exception ex) {
				super.unsubscribe(subscriber);
				throw ex;
			}
	}

	@Override
	public void unsubscribe(EventHandler subscriber) {
		super.unsubscribe(subscriber);
		if (noSubscriptions())
			unsubscribeFromAdaptee();
	}
	
	/**
	 * Call {@link #report()} when the adaptee is triggered.
	 */
	protected abstract void subscribeToAdaptee();
	protected abstract void unsubscribeFromAdaptee();

}
