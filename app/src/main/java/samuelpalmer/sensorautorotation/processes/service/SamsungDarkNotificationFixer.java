package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewStub;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.utilities.DarkThemeMonitor;

/**
 * On Samsung devices running Android 9 with night mode turned on, most notifications are given
 * a dark theme, but custom notifications stay in light theme. This makes our control notification
 * look glary and out-of-place. This class works around the problem by customising the notification
 * colours to match the Samsung ones.<br />
 * <br />
 * The following are known issues that we're considering out-of-scope:<br />
 * - The OS doesn't apply themes to custom notifications<br />
 * - The OS sometimes displays notifications in light theme on the lock screen regardless of night mode<br />
 * - The OS doesn't use our notification layout when the notification is minimised<br />
 * - Custom ROMs may colour notifications differently to official Samsung firmware<br />
 * - Colouring {@code "android:id/chronometer"} text since it uses a {@link ViewStub} and hence won't always be a {@link TextView}<br />
 */
public class SamsungDarkNotificationFixer {

    /**
     * Colour to use for the main text in the notification that isn't app-theme-coloured.
     * Corresponds to id {@code -104} in Samsung's {@code com.android.systemui.statusbar.NotificationColorPicker} class.
     */
    @ColorInt
    private static final int TEXT_COLOUR_MAIN = 0xFFFAFAFA;

    /**
     * Colour to use for text that isn't the main text in the notification.
     * Corresponds to id {@code -105} in Samsung's {@code com.android.systemui.statusbar.NotificationColorPicker} class.
     */
    @ColorInt
    private static final int TEXT_COLOUR_SECONDARY = 0xFFD0D0D0;

    private final Context context;

    @ColorInt
    private int notificationPrimaryColour;

    @IdRes
    private final int appNameTextId;

    @IdRes
    private final int headerTextSecondaryDividerId;

    @IdRes
    private final int headerTextSecondaryId;

    @IdRes
    private final int headerTextDividerId;

    @IdRes
    private final int headerTextId;

    @IdRes
    private final int timeDividerId;

    @IdRes
    private final int timeId;

    @IdRes
    private final int expandButtonId;

    @IdRes
    private final int overflowTextId;

    @IdRes
    private final int titleId;

    @IdRes
    private final int textLine1Id;

    @IdRes
    private final int textId;

    public SamsungDarkNotificationFixer(Context context) {
        this.context = context;

        notificationPrimaryColour = makePrimaryColour(context);

        appNameTextId = getId("android:id/app_name_text");

        headerTextSecondaryDividerId = getId("android:id/header_text_secondary_divider");
        headerTextSecondaryId = getId("android:id/header_text_secondary");
        headerTextDividerId = getId("android:id/header_text_divider");
        headerTextId = getId("android:id/header_text");
        timeDividerId = getId("android:id/time_divider");
        timeId = getId("android:id/time");

        expandButtonId = getId("android:id/expand_button");
        overflowTextId = getId("android:id/overflowText");

        titleId = getId("android:id/title");
        textLine1Id = getId("android:id/text_line_1");

        textId = getId("android:id/text");
    }

    /**
     * Samsung brightens the notification colours in dark theme. Algorithm taken from
     * {@code com.android.systemui.statusbar.NotificationData.getAppPrimaryColor()}.
     */
    @ColorInt
    private static int makePrimaryColour(Context context) {
        int rawColor = NotificationBuilderFactory.getNotificationColour(context);
        if (rawColor == 0)
            return 0xffa7b6be; // Colour -108 in Samsung's NotificationColorPicker class

        int a = 0xFF; // Always using max alpha since the OS does this as part of its notification colour sanitisation
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(rawColor), Color.green(rawColor), Color.blue(rawColor), hsv);
        hsv[2] = hsv[2] + 0.2f;
        hsv[2] = Math.max(0.0f, Math.min(1.0f, hsv[2]));
        return Color.HSVToColor(a, hsv);
    }

    private int getId(String qualifiedName) {
        return context.getResources().getIdentifier(qualifiedName, null, null);
    }

    public void applyTo(RemoteViews notification) {
        notification.setInt(R.id.notification_root, "setBackgroundColor", 0xFF252525);

        notification.setInt(android.R.id.icon, "setColorFilter", notificationPrimaryColour);
        notification.setTextColor(appNameTextId, notificationPrimaryColour);

        notification.setTextColor(headerTextSecondaryDividerId, TEXT_COLOUR_SECONDARY);
        notification.setTextColor(headerTextSecondaryId, TEXT_COLOUR_SECONDARY);
        notification.setTextColor(headerTextDividerId, TEXT_COLOUR_SECONDARY);
        notification.setTextColor(headerTextId, TEXT_COLOUR_SECONDARY);
        notification.setTextColor(timeDividerId, TEXT_COLOUR_SECONDARY);
        notification.setTextColor(timeId, TEXT_COLOUR_SECONDARY);

        notification.setInt(expandButtonId, "setColorFilter", notificationPrimaryColour);
        notification.setTextColor(overflowTextId, notificationPrimaryColour);

        notification.setTextColor(titleId, TEXT_COLOUR_MAIN);
        notification.setTextColor(textLine1Id, TEXT_COLOUR_MAIN);

        notification.setTextColor(textId, TEXT_COLOUR_SECONDARY);
    }

    @ColorInt
    public int getTitleColour() {
        return TEXT_COLOUR_MAIN;
    }

    @ColorInt
    public int getActionButtonColour() {
        return notificationPrimaryColour;
    }

    public static boolean isNeeded(Context context) {
        return Build.VERSION.SDK_INT == 28 && "samsung".equals(Build.BRAND) && DarkThemeMonitor.poll(context);
    }

}
