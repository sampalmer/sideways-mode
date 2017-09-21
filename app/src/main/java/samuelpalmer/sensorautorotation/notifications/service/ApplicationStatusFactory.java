package samuelpalmer.sensorautorotation.notifications.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class ApplicationStatusFactory {

	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screenRotationMonitor;
	
	public ApplicationStatusFactory(OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor) {
		this.calculator = calculator;
		this.screenRotationMonitor = screenRotationMonitor;
	}

	public Set<EventRegistrar> updateTriggers() {
		return new HashSet<>(
			Arrays.<EventRegistrar>asList(
				screenRotationMonitor, //For "overridden" detection
				calculator.onAnythingChanged //For "overridden" detection, user orientation changes, auto rotation mode changes, waiting on sensor changes
			)
		);
	}

	public ApplicationStatus generate() {
		return new ApplicationStatus(
			new UserOrientation(calculator.side(), screenRotationMonitor, calculator.proposedScreenOrientation()),
			calculator.isWaitingForSensor()
		);
	}
	
}
