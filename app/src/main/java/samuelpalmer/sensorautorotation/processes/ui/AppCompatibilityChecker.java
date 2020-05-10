package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.utilities.IntentExtensions;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;

/**
 * Prevents the user from opening an activity
 */
public class AppCompatibilityChecker implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Can't apply this to MessageActivity since that's the activity we use to display the error!
        if (activity instanceof MessageActivity)
            return;

        // Checking this in onStart() instead of onCreate() in case the user wants to go out and back in again to retry the check in case the problem is intermittent
        int startupErrorString = AppCompatibilityChecker.check(activity);
        if (startupErrorString != 0) {
            // Not letting the user go any further since they shouldn't have installed the app
            Log.w(getClass().getSimpleName(), activity.getText(startupErrorString).toString());
            Intent intent = MessageActivity.makeIntent(
                    activity,
                    R.drawable.image_error,
                    activity.getText(R.string.device_problem),
                    activity.getText(startupErrorString),
                    false
            );

            activity.startActivity(intent);

            activity.finish();
        }
    }

    /**
     * Determines whether the app will work on the current device.
     * Returns 0 on success. Otherwise returns explanation string resource.
     */
    @StringRes
    private static int check(Context context) {
        if (!OrientationSensorHandler.canDetectOrientation(context))
            return R.string.device_problem_explanation_sensor;

        if (Build.VERSION.SDK_INT >= 23) {
            Intent modifySystemSettingsIntent = SystemIntents.modifySystemSettings(context);
            if (IntentExtensions.activityDoesntExist(modifySystemSettingsIntent, context.getPackageManager()))
                return R.string.device_problem_explanation_permission;
        }

        return 0;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
