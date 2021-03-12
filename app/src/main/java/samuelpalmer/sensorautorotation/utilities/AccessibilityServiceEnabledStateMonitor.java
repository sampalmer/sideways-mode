package samuelpalmer.sensorautorotation.utilities;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.os.HandlerCompat;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Detects when an {@link AccessibilityService} gets enabled and disabled.
 */
public class AccessibilityServiceEnabledStateMonitor extends EventRegistrarAdaptor {

    private final static TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
    private final ComponentName targetService;
    private final ContentResolver contentResolver;
    
    private boolean isUpToDate = false;
    private boolean wasEnabled;
    
    public AccessibilityServiceEnabledStateMonitor(Context context, Class<? extends AccessibilityService> accessibilityService) {
        targetService = new ComponentName(context, accessibilityService);
        contentResolver = context.getContentResolver();
    }
    
    public boolean isEnabled() {
        if (isUpToDate)
            return wasEnabled;
        else
            return poll();
    }
    
    @Override
    protected void subscribeToAdaptee() {
        contentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES), false, observer);
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        contentResolver.unregisterContentObserver(observer);
        isUpToDate = false;
    }

    private final ContentObserver observer = new ContentObserver(HandlerCompat.createAsync(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            
            if (hasSubscriptions()) {
                boolean isEnabled = poll();
                if (!isUpToDate || isEnabled != wasEnabled) {
                    wasEnabled = isEnabled;
                    isUpToDate = true;
                    report();
                }
            }
        }
    };
    
    /**
     * Implementation mostly copied from the AccessibilityUtils Android class
     */
    private boolean poll() {
        String enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;
        
        colonSplitter.setString(enabledServicesSetting);
        
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);
            
            if (enabledService != null && enabledService.equals(targetService))
                return true;
        }
        
        return false;
    }
    
}
