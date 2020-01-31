package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        update(false); // Not sending an update since this is not a *change*, just the initial value
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        super.unsubscribeFromAdaptee();
        WindowChangeDetectingService.onActivityChange().unsubscribe(subscriber);
    }

    private final EventHandler subscriber = () -> update(true);

    private void update(boolean sendUpdate) {
        ComponentName currentActivity = WindowChangeDetectingService.getCurrentActivity();
        setTopActivity(currentActivity, sendUpdate);
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
        // TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
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

    @Nullable
    @Override
    public String makeAcquirePermissionCancelledMessage() {
        /*
            On some Android versions, the settings screen won't allow users to enable the accessibility service:
            On Android 4, the settings screen doesn't care about overlays.
            On Android 5, it blocks the button press with no explanation.
            On Android 6, it blocks the button press with a confusing message.
            On Android 7, it temporarily removes the overlay so the user can press the button.
         */
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 24)
            return context.getString(R.string.accessibility_service_cancelled);
        else
            return null;
    }
}
