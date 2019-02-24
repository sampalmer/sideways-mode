package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import androidx.annotation.NonNull;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.WindowChangeDetectingService;
import samuelpalmer.sensorautorotation.utilities.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

public final class AccessibilityServiceActivityMonitor extends ActivityMonitor {

    private final AccessibilityServiceEnabledStateMonitor enabledStateMonitor;

    AccessibilityServiceActivityMonitor(Context context) {
        super(context);
        enabledStateMonitor = new AccessibilityServiceEnabledStateMonitor(context, WindowChangeDetectingService.class);
    }

    @Override
    protected void subscribeToAdaptee() {
        super.subscribeToAdaptee();
        WindowChangeDetectingService.onActivityChange().subscribe(subscriber);

        // There's a chance that the accessibility service was already monitoring, so we'll check it just in case
        update();
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        super.unsubscribeFromAdaptee();
        WindowChangeDetectingService.onActivityChange().unsubscribe(subscriber);
    }

    private final EventHandler subscriber = this::update;

    private void update() {
        ComponentName currentActivity = WindowChangeDetectingService.getCurrentActivity();
        setTopActivity(currentActivity);
    }

    @Override
    public boolean hasPermission() {
        return enabledStateMonitor.isEnabled();
    }

    @Override
    @NonNull
    public EventRegistrarAdaptor onPermissionChange() {
        return enabledStateMonitor;
    }

    @Override
    public Intent makeAcquirePermissionIntent() {
        //TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    @Override
    public String makeAcquirePermissionQuestion() {
        return context.getString(R.string.permission_needed_accessibility_service_summary);
    }

    @Override
    public String makeAcquirePermissionExplanation() {
        CharSequence appNameFull = context.getText(R.string.app_name_full);
        return context.getString(R.string.permission_needed_accessibility_service_description, appNameFull);
    }

}
