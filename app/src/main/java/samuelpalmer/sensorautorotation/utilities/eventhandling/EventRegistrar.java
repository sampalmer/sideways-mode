package samuelpalmer.sensorautorotation.utilities.eventhandling;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an event that can be listened to for callbacks.
 */
public abstract class EventRegistrar {
    private final Set<EventHandler> subscriptions = new HashSet<>();
    
    public void subscribe(EventHandler subscriber) {
        if (subscribed(subscriber))
            throw new RuntimeException("Already subscribed: " + subscriber);

        subscriptions.add(subscriber);
    }
    
    /**
     * Unsubscribes the given subscriber. Guarantees that the subscriber will not be called after this call completes.
     */
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
            /*
            It's possible that the last subscriber unsubscribed *another* subscriber as part of its callback.
            This is bad because the just-unsubscribed subscriber is about to be called.
            So we'll check for that here and abort as a courtesy.
            */
            if (subscriptions.contains(subscriber))
                subscriber.update();
    }
    
    protected boolean hasSubscriptions() {
        return !subscriptions.isEmpty();
    }
}
