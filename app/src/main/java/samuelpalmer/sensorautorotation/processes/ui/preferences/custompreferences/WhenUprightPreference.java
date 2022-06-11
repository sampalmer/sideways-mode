package samuelpalmer.sensorautorotation.processes.ui.preferences.custompreferences;

import android.content.Context;
import android.util.AttributeSet;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Preference for the {@link ApplicationSettings#KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT} setting
 */
public class WhenUprightPreference extends BooleanListPreference {

    public WhenUprightPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise(context);
    }

    public WhenUprightPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    public WhenUprightPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    public WhenUprightPreference(Context context) {
        super(context);
        initialise(context);
    }

    private void initialise(Context context) {
        setKey(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT);
        setTitle(R.string.preference_when_upright);
        setDialogTitle(R.string.preference_when_upright);
        setIcon(R.drawable.preference_upright);

        setDefaultValue(booleanToString(true));

        String keepNotificationText = context.getString(R.string.preference_when_upright_keep_notification);
        String closeNotificationText = context.getString(R.string.preference_when_upright_close_notification);

        setEntries(new CharSequence[]{ keepNotificationText, closeNotificationText });
        setEntryValues(new CharSequence[]{ booleanToString(false), booleanToString(true) });

        // Show the current selection in the summary
        setSummaryProvider(SimpleSummaryProvider.getInstance());
    }

}
