package samuelpalmer.sensorautorotation.notifications.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;

public class ApplicationStatusFactory {

	private final OrientationCalculator calculator;
	
	public ApplicationStatusFactory(OrientationCalculator calculator) {
		this.calculator = calculator;
	}

	public Set<EventRegistrar> updateTriggers() {
		//noinspection ArraysAsListWithZeroOrOneArgument
		return new HashSet<>(
			Arrays.<EventRegistrar>asList(
				calculator.onAnythingChanged //User orientation changes, waiting on sensor changes
			)
		);
	}

	public ApplicationStatus generate() {
		return new ApplicationStatus(
			calculator.side(),
			calculator.isWaitingForSensor()
		);
	}
	
}
