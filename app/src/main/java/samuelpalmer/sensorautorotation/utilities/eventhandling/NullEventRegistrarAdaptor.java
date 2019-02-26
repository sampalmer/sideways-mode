package samuelpalmer.sensorautorotation.utilities.eventhandling;

/**
 * Does nothing. Use this when you require an {@link EventRegistrarAdaptor} but don't need it to do anything.
 */
public class NullEventRegistrarAdaptor extends EventRegistrarAdaptor {

    @Override
    protected void subscribeToAdaptee() {

    }

    @Override
    protected void unsubscribeFromAdaptee() {

    }

}
