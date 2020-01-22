package samuelpalmer.sensorautorotation.processes.ui.preferences.custompreferences;

import android.content.Context;
import android.util.AttributeSet;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class WhenLockedPreference extends BooleanDropDownPreference {

    public WhenLockedPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise(context);
    }

    public WhenLockedPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    public WhenLockedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    public WhenLockedPreference(Context context) {
        super(context);
        initialise(context);
    }

    private void initialise(Context context) {
        setKey(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED);
        setTitle(R.string.preference_when_screen_locked);
        setIcon(R.drawable.preference_device_locked);

        setDefaultValue(booleanToString(true));

        String doNothingText = context.getString(R.string.preference_when_screen_locked_do_nothing);
        String appNameText = context.getString(R.string.app_name_full);
        String turnOffText = context.getString(R.string.preference_when_screen_locked_turn_off, appNameText);

        setEntries(new CharSequence[]{ doNothingText, turnOffText});
        setEntryValues(new CharSequence[]{ booleanToString(false), booleanToString(true) });

        // Show the current selection in the summary
        setSummary("%s");
    }

}
