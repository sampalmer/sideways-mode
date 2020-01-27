package samuelpalmer.sensorautorotation.utilities;

import android.content.Context;
import android.content.res.Configuration;

public class ConfigurationUtilities {

    private ConfigurationUtilities() {}

    public static boolean isDarkTheme(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

}
