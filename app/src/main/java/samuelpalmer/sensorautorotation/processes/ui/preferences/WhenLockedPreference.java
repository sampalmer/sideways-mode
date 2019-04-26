package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.DropDownPreference;
import samuelpalmer.sensorautorotation.R;

public class WhenLockedPreference extends DropDownPreference {

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
        setIcon(R.drawable.menu_power);

        setDefaultValue(booleanToString(true));

        String doNothingText = context.getString(R.string.preference_when_screen_locked_do_nothing);
        String appNameText = context.getString(R.string.app_name_full);
        String turnOffText = context.getString(R.string.preference_when_screen_locked_turn_off, appNameText);

        setEntries(new CharSequence[]{ doNothingText, turnOffText});
        setEntryValues(new CharSequence[]{ booleanToString(false), booleanToString(true) });

        // Show the current selection in the summary
        setSummary("%s");
    }

    @Override
    protected boolean persistString(String value) {
        boolean booleanValue = stringToBoolean(value);

        return persistBoolean(booleanValue);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (defaultReturnValue == null)
            throw new IllegalArgumentException("Cannot convert default value 'null' to boolean value");

        boolean booleanDefaultReturnValue = stringToBoolean(defaultReturnValue);
        boolean booleanValue = getPersistedBoolean(booleanDefaultReturnValue);
        return booleanToString(booleanValue);
    }

    private static boolean stringToBoolean(String value) {
        switch (value) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new IllegalArgumentException("Unrecognised boolean value: " + value);
        }
    }

    private static String booleanToString(boolean booleanValue) {
        return booleanValue ? "true" : "false";
    }

}
