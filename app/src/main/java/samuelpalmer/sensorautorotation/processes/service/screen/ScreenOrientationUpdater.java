package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationUpdater extends State {

    private final OrientationCalculator calculator;
    private final ScreenOrientationController screen;
    
    public ScreenOrientationUpdater(Context context, OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor) {
        this.calculator = calculator;
        this.screen = new ScreenOrientationController(context, screenRotationMonitor);
    }

    @Override
    protected void start() {
        screen.ensureStarted();
        calculator.onAnythingChanged.subscribe(applyChange);
    }

    @Override
    protected void stop() {
        if (screen.wasSystemRotationOn() || Build.VERSION.SDK_INT >= 28) {
            // Once the screen remained in the wrong orientation after turning the app off on my Motorola Moto G5 plus.
            // It seemed that system auto-rotation didn't kick in straight away, or maybe it was actually waiting for the next device rotation.
            // So we'll return the screen to the appropiate orientation for the user in the upright position before turning system auto-rotation back on.
            //
            // Also, Android P lets users manually change the screen orientation,
            // so we'll leave it set to the most appropriate one for the upright position since we can assume the user is now upright.
            screen.propose(calculator.proposedScreenOrientationForUser(Orientation.NATURAL));
        }
        
        calculator.onAnythingChanged.unsubscribe(applyChange);
        screen.ensureStopped();
    }
    
    private final EventHandler applyChange = this::propose;

    private void propose() {
        Orientation orientation = calculator.proposedScreenOrientation();
        
        if (!orientation.equals(lastProposed)) {
            lastProposed = orientation;
            screen.propose(orientation);
        }
    }
    
    @Nullable
    private Orientation lastProposed;
    
}
