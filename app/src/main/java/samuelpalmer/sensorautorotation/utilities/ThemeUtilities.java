package samuelpalmer.sensorautorotation.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public class ThemeUtilities {

    private ThemeUtilities(){}

    /**
     * Gets the colour for the given attribute from the current theme
     */
    @ColorInt
    public static int getThemeColour(Context context, @AttrRes int attribute) {
        Resources.Theme theme = context.getTheme();

        TypedValue outValue = new TypedValue();
        if (!theme.resolveAttribute(attribute, outValue, true))
            throw new RuntimeException("Can't find theme color for " + attribute);

        @ColorRes int colorRes = outValue.resourceId;
        if (colorRes == 0)
            throw new RuntimeException("Theme color " + attribute + " is not a resource");

        @ColorInt int color = ContextCompat.getColor(context, colorRes);
        return color;
    }

}
