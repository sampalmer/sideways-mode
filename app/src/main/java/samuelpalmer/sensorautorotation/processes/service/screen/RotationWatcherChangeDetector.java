package samuelpalmer.sensorautorotation.processes.service.screen;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Screen;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.screen.RotationWatcherRegistrar.RotationWatcherNotSupportedException;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor.Callback;

/**
 * Supports detection of all four screen rotations using a hidden API
 */
public class RotationWatcherChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final ScreenRotationMonitor.Callback callback;
	private final Screen screen;
	private final RotationWatcherRegistrar watcher;

	public RotationWatcherChangeDetector(Callback callback, Screen screen) throws RotationWatcherNotSupportedException {
		this.callback = callback;
		this.screen = screen;
		watcher = RotationWatcherRegistrar.instance();
	}

	@Override
	public void start() throws RotationWatcherNotSupportedException {
		watcher.subscribe(subscriber);
	}

	@Override
	public void stop() throws RotationWatcherNotSupportedException {
		watcher.unsubscribe(subscriber);
	}

	@Override
	public Orientation currentRotation() throws RotationWatcherNotSupportedException {
		if (watcher.lastRotationOrNull() != null)
			return watcher.lastRotationOrNull();
		else
			return screen.realOrientation();
	}

	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			callback.changed();
		}
	};



}
