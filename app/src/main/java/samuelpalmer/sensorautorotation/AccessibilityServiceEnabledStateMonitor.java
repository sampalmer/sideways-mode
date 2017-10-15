package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;

public class AccessibilityServiceEnabledStateMonitor extends EventRegistrarAdaptor {

	private final static TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
	private final ComponentName expected;
	private final ContentResolver contentResolver;

	public AccessibilityServiceEnabledStateMonitor() {
		expected = new ComponentName(CurrentProcess.context(), WindowChangeDetectingService.class);
		contentResolver = CurrentProcess.context().getContentResolver();
	}

	/**
	 * Implementation mostly copied from the AccessibilityUtils Android class
	 */
	public boolean isEnabled() {
		String enabledServicesSetting = Settings.Secure.getString(contentResolver,  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
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


	@Override
	protected void subscribeToAdaptee() {
		contentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, observer);
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		contentResolver.unregisterContentObserver(observer);
	}

	private final ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			report();
		}
	};

}
