package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

import samuelpalmer.common.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;

public class AccessibilityServiceEnabledStateChecker {

	private final AccessibilityManager accessibilityManager;

	public AccessibilityServiceEnabledStateChecker() {
		accessibilityManager = SystemServices.accessibilityManager();
	}

	public boolean isEnabled() {
		ComponentName expectedName = new ComponentName(Process.context(), WindowChangeDetectingService.class);

		//TODO: Put this on StackOverflow; it seems simpler and more reliable than the answers already out there
		List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
		for (AccessibilityServiceInfo enabledService : enabledServices) {
			ComponentName serviceName = ComponentNameExtensions.makeFrom(enabledService.getResolveInfo().serviceInfo);
			if (serviceName.equals(expectedName))
				return true;
		}

		return false;
	}


}
