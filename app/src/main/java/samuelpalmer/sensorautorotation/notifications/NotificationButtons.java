package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;

public class NotificationButtons {

    // Opacity values taken from https://material.io/archive/guidelines/style/color.html#color-usability
    // For API 28 - 29, they were reverse-engineered from the emulator images
    private static final float OPACITY_DARK_ICON_ACTIVE = 0.54f;
    private static final float OPACITY_DARK_ICON_INACTIVE = VERSION.SDK_INT >= 28 ? 0.54f : 0.38f;
    private static final float OPACITY_LIGHT_ICON_ACTIVE = 1.00f;
    private static final float OPACITY_LIGHT_ICON_INACTIVE = VERSION.SDK_INT >= 28 ? 0.70f : 0.50f;

    private final RemoteViews content;

    @ColorInt
    private final int buttonColour;

    private final Context context;
    private final boolean isGreyscale;
    private final boolean isDarkTheme;

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

        isGreyscale = Color.red(buttonColour) == Color.green(buttonColour) && Color.red(buttonColour) == Color.blue(buttonColour);

        // If the title text is bright then the background must be dark
        isDarkTheme = ColorUtils.calculateLuminance(titleColour) >= 0.5;
    }

    /**
     * @param icon 32 dip x 32 dip (including padding), white
     */
    public NotificationButtons configureButton(int containerId, int buttonId, int icon, int pendingIntentCode, Intent onClickIntent, CharSequence contentDescription, boolean selected) {
        @ColorInt int backgroundColour;
        @ColorInt int iconColour;

        if (isGreyscale) {
            if (isDarkTheme) {
                // API 15 - 19 stock
                if (selected) {
                    // Using 12% of the primary colour as suggested by https://material.io/design/color/dark-theme.html#states
                    backgroundColour = ColorUtils.setAlphaComponent(Color.WHITE, Math.round(0xFF * 0.12f));
                    iconColour = ColorUtils.setAlphaComponent(Color.WHITE, Math.round(0xFF * OPACITY_LIGHT_ICON_ACTIVE));
                } else { // Not selected
                    backgroundColour = Color.TRANSPARENT;
                    iconColour = ColorUtils.setAlphaComponent(Color.WHITE, Math.round(0xFF * OPACITY_LIGHT_ICON_INACTIVE));
                }
            } else { // Light theme
                // API 21 - 23 stock
                if (selected) {
                    // Using 12% of the primary colour as suggested by https://material.io/design/interaction/states.html#activated
                    backgroundColour = ColorUtils.setAlphaComponent(Color.BLACK, Math.round(0xFF * 0.12f));
                    iconColour = ColorUtils.setAlphaComponent(Color.BLACK, Math.round(0xFF * OPACITY_DARK_ICON_ACTIVE));
                } else { // Not selected
                    backgroundColour = Color.TRANSPARENT;
                    iconColour = ColorUtils.setAlphaComponent(Color.BLACK, Math.round(0xFF * OPACITY_DARK_ICON_INACTIVE));
                }
            }
        } else { // Colour
            if (isDarkTheme) {
                // API 29
                if (selected) {
                    // Using 12% of the primary colour as suggested by https://material.io/design/color/dark-theme.html#states
                    // TODO: The selected item doesn't stand out much in this situation. Investigate other options.
                    backgroundColour = ColorUtils.setAlphaComponent(buttonColour, Math.round(Color.alpha(buttonColour) * 0.12f));
                    iconColour = buttonColour;
                } else { // Not selected
                    backgroundColour = Color.TRANSPARENT;
                    iconColour = ColorUtils.setAlphaComponent(Color.WHITE, Math.round(0xFF * OPACITY_LIGHT_ICON_INACTIVE));
                }
            } else { // Light theme
                // API 24 - 29
                if (selected) {
                    // Using 12% of the primary colour as suggested by https://material.io/design/interaction/states.html#activated
                    backgroundColour = ColorUtils.setAlphaComponent(buttonColour, Math.round(Color.alpha(buttonColour) * 0.12f));
                    iconColour = buttonColour;
                } else { // Not selected
                    backgroundColour = Color.TRANSPARENT;
                    iconColour = ColorUtils.setAlphaComponent(Color.BLACK, Math.round(0xFF * OPACITY_DARK_ICON_INACTIVE));
                }
            }
        }

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

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

}
