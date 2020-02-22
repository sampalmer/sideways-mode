package samuelpalmer.sensorautorotation.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;

/**
 * Convenience methods for creating intents for OS tasks/activities.
 */
public abstract class SystemIntents {
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Intent modifySystemSettings(Context context) {
        return new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri(context));
    }
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Intent drawOnTopOfApps(Context context) {
        // Sending the user straight to the permissions screen for our app.
        // Starting from Android 11, the package is ignored and the user will still have to select the app.
        return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri(context));
    }

    private static Uri packageUri(Context context) {
        return Uri.fromParts("package", context.getPackageName(), null);
    }
    
}
