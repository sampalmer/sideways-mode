package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.CurrentProcessInfo;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class ServiceProcess {

	public static boolean isServiceProcess() {
		String currentProcessName = CurrentProcessInfo.currentProcessName();
		if (currentProcessName.equals(serviceProcess()))
			return true;
		else if (currentProcessName.equals(uiProcess()))
			return false;
		else
			throw new RuntimeException("Current process has unrecognised name: " + currentProcessName);
	}

	private static String uiProcess() {
		return packageName();
	}

	private static CharSequence serviceProcess() {
		return packageName() + StringResource.get(R.string.service_process_name);
	}

	private static String packageName() {
		return CurrentProcess.context().getPackageName();
	}

}
