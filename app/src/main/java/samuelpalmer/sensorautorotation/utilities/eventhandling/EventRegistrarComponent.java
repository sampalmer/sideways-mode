package samuelpalmer.sensorautorotation.utilities.eventhandling;

/**
 * This is basically an {@link EventRegistrarAdaptor} for situations where you cannot inherit from it.
 */
public final class EventRegistrarComponent extends EventRegistrarAdaptor {

    public final UpdatingEventRegistrar onSubscriptionsChange = new UpdatingEventRegistrar();

    @Override
    protected void subscribeToAdaptee() {
        onSubscriptionsChange.report();
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        onSubscriptionsChange.report();
    }

    @Override
    public boolean hasSubscriptions() {
        return super.hasSubscriptions();
    }

    @Override
    public void report() {
        super.report();
    }

}
