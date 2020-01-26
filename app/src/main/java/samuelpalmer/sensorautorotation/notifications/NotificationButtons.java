package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.graphics.ColorUtils;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;

/**
 * Assembles a custom notification layout that includes inline buttons in the main notification
 * content. This is more convenient for the user than stock notification actions because the
 * buttons are always visible and don't increase the height of the notification.
 */
public class NotificationButtons {

    private final RemoteViews content;

    @ColorInt
    private final int buttonColour;

    private final Context context;

    /**
     * Whether the notification is dark-themed or light-themed. We need this to figure out how to
     * colour the inline notification buttons.
     *
     * Notifications are generally dark-themed in API 15 - 19, and also in API 29 if dark theme is
     * enabled in the system settings.
     */
    private final Theme theme;

    /**
     * Whether the OS colours the stock notification action buttons. This is a useful guideline for
     * whether to colour our custom inline buttons.
     *
     * Notifications were mostly monochrome until API 21, when the notification icon was coloured.
     * Notification buttons were mostly monochrome until API 24, when they were coloured to match
     * the colour supplied by the app.
     */
    private final boolean isColour;

    /**
     * @param originalNotification Get this from the notification builder.
     * @param titleColour Get this from {@link NotificationStyles#getTitleColour()}
     * @param buttonColour Get this from {@link NotificationStyles#getActionButtonColour()}
     */
    public NotificationButtons(Context context, int layoutResource, RemoteViews originalNotification, @ColorInt int titleColour, @ColorInt int buttonColour) {
        this.context = context;

        content = new RemoteViews(context.getPackageName(), layoutResource);
        content.removeAllViews(R.id.notification_content);
        content.addView(R.id.notification_content, originalNotification);

        this.buttonColour = buttonColour;

        isColour = Color.red(buttonColour) != Color.green(buttonColour) || Color.red(buttonColour) != Color.blue(buttonColour);

        // If the title text is bright then the background must be dark
        theme = ColorUtils.calculateLuminance(titleColour) >= 0.5 ? Theme.DARK : Theme.LIGHT;
    }

    /**
     * @param icon 32 dip x 32 dip (including padding), white
     */
    public NotificationButtons configureButton(int containerId, int buttonId, int icon, int pendingIntentCode, Intent onClickIntent, CharSequence contentDescription, boolean selected) {
        @ColorInt int backgroundColour = getButtonBackgroundColour(selected);
        @ColorInt int iconColour = getIconColour(selected);

        content.setInt(containerId, "setBackgroundColor", backgroundColour);
        content.setImageViewResource(buttonId, icon);

        // The colour filter currently ignores alpha, but we're resetting the alpha to 1.0 anyway just in case this behaviour changes a future version of the OS
        content.setInt(buttonId, "setColorFilter", ColorUtils.setAlphaComponent(iconColour, 0xFF));

        // In API 15, the method is called "setAlpha", but it was deprecated in API 16
        content.setInt(buttonId, VERSION.SDK_INT >= 16 ? "setImageAlpha" : "setAlpha", Color.alpha(iconColour));

        content.setOnClickPendingIntent(buttonId, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));

        if (VERSION.SDK_INT >= 15)
            content.setContentDescription(buttonId, contentDescription);
        
        return this;
    }

    @ColorInt
    private int getIconColour(boolean isButtonSelected) {
        if (isColour && isButtonSelected) {
            return buttonColour;
        } else {
            float iconOpacity = isButtonSelected ? theme.iconOpacityActive : theme.iconOpacityInactive;
            return ColorUtils.setAlphaComponent(theme.opaqueIconColour, Math.round(0xFF * iconOpacity));
        }
    }

    @ColorInt
    private int getButtonBackgroundColour(boolean isButtonSelected) {
        if (!isButtonSelected)
            return Color.TRANSPARENT;
        else { // Selected
            @ColorInt int backgroundColourOpaque = isColour ? buttonColour : theme.opaqueIconColour;

            // Using 12% of the primary colour as suggested by https://material.io/design/color/dark-theme.html#states and https://material.io/design/interaction/states.html#activated
            return ColorUtils.setAlphaComponent(backgroundColourOpaque, Math.round(Color.alpha(backgroundColourOpaque) * 0.12f));
        }
    }

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

    private enum Theme {
        // Opacity values taken from https://material.io/archive/guidelines/style/color.html#color-usability
        // For API 28 - 29, they were reverse-engineered from the emulator images

        DARK(Color.WHITE, 1.00f, VERSION.SDK_INT >= 28 ? 0.70f : 0.50f),
        LIGHT(Color.BLACK, 0.54f, VERSION.SDK_INT >= 28 ? 0.54f : 0.38f);

        /**
         * Icon colour for this theme before applying opacity
         */
        @ColorInt
        public final int opaqueIconColour;

        @FloatRange(from = 0, to = 1)
        public final float iconOpacityActive;

        @FloatRange(from = 0, to = 1)
        public final float iconOpacityInactive;

        Theme(
                @ColorInt int opaqueIconColour,
                @FloatRange(from = 0, to = 1) float iconOpacityActive,
                @FloatRange(from = 0, to = 1) float iconOpacityInactive
        ) {
            this.opaqueIconColour = opaqueIconColour;
            this.iconOpacityActive = iconOpacityActive;
            this.iconOpacityInactive = iconOpacityInactive;
        }
    }

}
