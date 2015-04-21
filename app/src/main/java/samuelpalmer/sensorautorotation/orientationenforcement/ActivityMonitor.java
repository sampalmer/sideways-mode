package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.orientationenforcement.currentapplicationretrieval.CurrentApplicationRetriever;

public class ActivityMonitor extends State {

	private final RecurringRunner recurringRunner;
	private final UpdatingEventRegistrar<Set<String>> onChange = new UpdatingEventRegistrar<>();
	private final CurrentApplicationRetriever currentApplicationRetriever;
	private Set<String> lastPackages = new HashSet<>(0);

	public ActivityMonitor(Context context) {
		//Alternatively, use a Timer to run in a different thread.
		recurringRunner = new RecurringRunner(updateEnforcement, 333);
		currentApplicationRetriever = CurrentApplicationRetriever.create(context);
	}

	public void start() {
		recurringRunner.start();
	}

	public void stop() {
		recurringRunner.stop();
	}

	private final Runnable updateEnforcement = new Runnable() {

		@Override
		public void run() {

			Set<String> currentPackages = new HashSet<>(Arrays.asList(currentApplicationRetriever.getPackages()));

			if (!currentPackages.equals(lastPackages)) {
				lastPackages = currentPackages;
				onChange.report(currentPackages);
			}
		}

	};

	public EventRegistrar<Set<String>> onChange() {
		return onChange;
	}
}
