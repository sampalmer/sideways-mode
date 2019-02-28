package samuelpalmer.sensorautorotation.utilities;

import android.app.Activity;
import android.content.Intent;

public class ActivityUtils {

    public static void finishActivitiesOnTopOf(Activity currentActivity) {
        Intent finishPermissionActivityIntent = new Intent(currentActivity, currentActivity.getClass())
            // This flag finishes all activities that are on top of this activity
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // This flag prevents this activity from being recreated
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // This has the side-effect of bringing this activity's task to the front, but that shouldn't matter
        // in practice since we're optimising for the most common scenarios.
        currentActivity.startActivity(finishPermissionActivityIntent);

        // The other way to do this is using finishActivity(), but it doesn't work with the usage settings activity,
        // and it also doesn't work with activities started from fragments.
    }

}
