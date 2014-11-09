package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.landscapetriggering.Listener;
import samuelpalmer.sensorautorotation.landscapetriggering.Monitor;
import samuelpalmer.sensorautorotation.landscapetriggering.MonitorFactory;


public class OrientationCalculator {
	
	private Orientation device;
	
	private MonitorFactory monitorFactory;
	private Screen screen;
	
	public OrientationCalculator(MonitorFactory monitorFactory, Screen screen) {
		this.monitorFactory = monitorFactory;
		this.screen = screen;
	}
	
	public void setDevice(Orientation newDevice) throws EnvironmentException {
		
		if (device != null) {
			Rotation deviceChange = newDevice.minus(device);
			
			if (deviceChange.isNinetyDegree()) {
				final Orientation currentScreenOrientation = screen.get();
				final Orientation proposedScreenOrientation = currentScreenOrientation.plus(deviceChange);
				
				if (proposedScreenOrientation.sideways()) {
					propose(proposedScreenOrientation);
				}
				else {
					screen.set(proposedScreenOrientation);
					propose(currentScreenOrientation);
				}
			}
		}
		
		device = newDevice;
	}

	private Monitor monitor;
	
	private void propose(final Orientation proposedScreenOrientation) throws EnvironmentException {
		ensureNotMonitoring();
		
		monitor = monitorFactory.create(new Listener() {
			
			@Override
			public void trigger() throws EnvironmentException {
				screen.set(proposedScreenOrientation);
			}

			@Override
			public void stop() {
				monitor = null;
			}
			
		});
	}
	
	public void pause() {
		ensureNotMonitoring();
	}
	
	private void ensureNotMonitoring() {
		if (monitor != null)
			monitor.stop();
	}
	
	public void resume() {}
	
}
