package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.ComponentName;
import android.provider.Settings;
import android.text.TextUtils;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;

public class AccessibilityServiceEnabledStateChecker {

	private final static TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
	private final ComponentName expected;

	public AccessibilityServiceEnabledStateChecker() {
		expected = new ComponentName(Process.context(), WindowChangeDetectingService.class);
	}

	//TODO: Add support for subscribing to notifications for change of enabled state

	/**
	 * Implementation mostly copied from the AccessibilityUtils Android class
	 */
	public boolean isEnabled() {
		String enabledServicesSetting = Settings.Secure.getString(Process.context().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		if (enabledServicesSetting == null)
			return false;

		TextUtils.SimpleStringSplitter colonSplitter = sStringColonSplitter;
		colonSplitter.setString(enabledServicesSetting);

		while (colonSplitter.hasNext()) {
			String componentNameString = colonSplitter.next();
			ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

			if (enabledService != null && enabledService.equals(expected))
				return true;
		}

		return false;
	}


}
