package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

class ApplicationStatusMonitor extends EventRegistrarAdaptor {
    
    private final SharedPreferences prefs;
    
    private boolean subscribedToRunningStateUpdates = false;
    
    public ApplicationStatusMonitor(Context context) {
        prefs = ServiceSettings.getSharedPreferences(context);
    }
    
    @Nullable
    public ApplicationStatus getStatus() {
        if (ServiceRunningStateHolder.isServiceRunning()) {
            ApplicationStatus.RunningState runningState = ServiceRunningStateHolder.getServiceInstance().runningStateUpdater.getLastStatusRunningState();
            if (runningState == null)
                return null;
            else
                return new ApplicationStatus(runningState, null);
        }
        else {
            String errorJson = prefs.getString(ServiceSettings.KEY_LAST_ERROR_JSON, null);
            
            return new ApplicationStatus(
                null,
                errorJson == null ? null : EnvironmentError.fromJson(errorJson)
            );
        }
    }
    
    @Override
    protected void subscribeToAdaptee() {
        ServiceRunningStateHolder.onStartedOrStopped.subscribe(enabledStateChanged);
        prefs.registerOnSharedPreferenceChangeListener(errorChanged);
        syncRunningStateSubscription();
    }
    
    @Override
    protected void unsubscribeFromAdaptee() {
        syncRunningStateSubscription();
        prefs.unregisterOnSharedPreferenceChangeListener(errorChanged);
        ServiceRunningStateHolder.onStartedOrStopped.unsubscribe(enabledStateChanged);
    }
    
    private final EventHandler enabledStateChanged = () -> {
        syncRunningStateSubscription();
        
        // Not reporting a change if the app has just been turned on since we want to wait for the first running state update instead.
        if (!ServiceRunningStateHolder.isServiceRunning())
            report();
    };
    
    private void syncRunningStateSubscription() {
        if (ServiceRunningStateHolder.isServiceRunning()) {
            boolean shouldBeSubscribedToRunningStateUpdates = hasSubscriptions();
            if (subscribedToRunningStateUpdates != shouldBeSubscribedToRunningStateUpdates) {
                if (shouldBeSubscribedToRunningStateUpdates) {
                    ServiceRunningStateHolder.getServiceInstance().runningStateUpdater.onStateChanged.subscribe(runningStateChanged);
                    subscribedToRunningStateUpdates = true;
                }
                else {
                    ServiceRunningStateHolder.getServiceInstance().runningStateUpdater.onStateChanged.unsubscribe(runningStateChanged);
                    subscribedToRunningStateUpdates = false;
                }
            }
        }
        else {
            // If the service stopped running, we were implicitly unsubscribed
            subscribedToRunningStateUpdates = false;
        }
    }
    
    private final SharedPreferences.OnSharedPreferenceChangeListener errorChanged = (sharedPreferences, s) -> {
        if (hasSubscriptions() && s.equals(ServiceSettings.KEY_LAST_ERROR_JSON))
            enabledStateChanged.update();
    };
    
    private final EventHandler runningStateChanged = this::report;
    
}
