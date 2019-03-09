package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class RunningStateUpdater extends State {
    
    private final OrientationCalculator calculator;
    private final RotationService service;
    private final ServiceNotification notificationBuilder;
    
    public RunningStateUpdater(Context context, OrientationCalculator calculator, RotationService service) {
        this.calculator = calculator;
        this.service = service;
        notificationBuilder = new ServiceNotification(context);
    }

    @Override
    protected void start() {
        updateDisplays.update();
        calculator.onAnythingChanged.subscribe(updateDisplays);
    }

    @Override
    protected void stop() {
        calculator.onAnythingChanged.unsubscribe(updateDisplays);
    }

    private final EventHandler updateDisplays = new EventHandler() {
        @Override
        public void update() {
            ApplicationStatus.RunningState runningState = calculator.runningState();

            if (!runningState.equals(lastRunningState)) {
                Notification notification = notificationBuilder.build(runningState);
                
                // Running the service in the foreground reduces the chance of the process being killed.
                // This is important, since the process being killed would result in the screen no longer rotating.
                service.startForeground(NotificationIds.SERVICE, notification);
                
                lastRunningState = runningState;
                onStateChanged.report();
            }
        }
    };
    
    @Nullable
    public ApplicationStatus.RunningState getLastStatusRunningState() {
        return lastRunningState;
    }
    
    public final UpdatingEventRegistrar onStateChanged = new UpdatingEventRegistrar();
    
    private ApplicationStatus.RunningState lastRunningState;
}
