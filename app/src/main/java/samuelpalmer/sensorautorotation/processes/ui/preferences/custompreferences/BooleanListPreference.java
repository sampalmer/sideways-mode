package samuelpalmer.sensorautorotation.processes.ui.preferences.custompreferences;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.ListPreference;

/**
 * Like {@link ListPreference} but uses boolean values for the entry values.
 * When getting or setting entry values, use {@link #stringToBoolean(String)} and {@link #booleanToString(boolean)}
 * to convert them to and from boolean values, respectively.
 *
 * Not using {@link androidx.preference.DropDownPreference} because it uses a legacy spinner that
 * doesn't get themed properly in dark mode, plus it looks like it's not used anymore in the OS
 * settings app, and it's not even mentioned in https://developer.android.com/guide/topics/ui/settings/components-and-attributes.
 */
@SuppressWarnings("WeakerAccess")
public class BooleanListPreference extends ListPreference {

    public BooleanListPreference(Context context) {
        super(context);
    }

    public BooleanListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BooleanListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BooleanListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected boolean persistString(String value) {
        boolean booleanValue = stringToBoolean(value);
        return persistBoolean(booleanValue);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        boolean booleanValue;

        if (defaultReturnValue != null) {
            boolean booleanDefaultReturnValue = stringToBoolean(defaultReturnValue);
            booleanValue = getPersistedBoolean(booleanDefaultReturnValue);
        } else {
            // We haven't been given a default return value, but we need to specify one when retrieving the boolean

            if (getPersistedBoolean(true) == getPersistedBoolean(false)) {
                // The default value is being ignored, so we're good to go
                booleanValue = getPersistedBoolean(true);
            } else {
                // We need a default value but haven't been given one, so we're stuck
                throw new IllegalArgumentException("Cannot get a boolean without a default return value");
            }
        }

        return booleanToString(booleanValue);
    }

    public static boolean stringToBoolean(String value) {
        switch (value) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new IllegalArgumentException("Unrecognised boolean value: " + value);
        }
    }

    public static String booleanToString(boolean booleanValue) {
        return booleanValue ? "true" : "false";
    }

}
