package samuelpalmer.sensorautorotation.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
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
    public NotificationButtons configureButton(
            @IdRes int containerId,
            @IdRes int buttonId,
            @IdRes int progressOnColourDarkThemeId,
            @IdRes int progressOnColourLightThemeId,
            @IdRes int progressSelectedDarkThemeId,
            @IdRes int progressSelectedLightThemeId,
            @DrawableRes int icon,
            int pendingIntentCode,
            Intent onClickIntent,
            CharSequence contentDescription,
            boolean selected,
            boolean isLoading
    ) {
        @ColorInt int backgroundColour = getButtonBackgroundColour(selected);

        content.setInt(containerId, "setBackgroundColor", backgroundColour);

        // Not hiding the button altogether when loading since then the user can click the notification body behind it
        content.setImageViewResource(buttonId, isLoading ? 0 : icon);

        // Unfortunately, the progress bar colour cannot be changed directly, so we'll switch between pre-defined progress bars to achieve the same effect
        content.setViewVisibility(progressOnColourDarkThemeId, View.GONE);
        content.setViewVisibility(progressOnColourLightThemeId, View.GONE);
        content.setViewVisibility(progressSelectedDarkThemeId, View.GONE);
        content.setViewVisibility(progressSelectedLightThemeId, View.GONE);

        @ColorInt int iconColour;
        if (isColour && selected) {
            iconColour = ContextCompat.getColor(context, theme.colourOnButtonColour);
            if (isLoading)
                content.setViewVisibility(theme == Theme.DARK ? progressOnColourDarkThemeId : progressOnColourLightThemeId, View.VISIBLE);
        } else {
            iconColour = ContextCompat.getColor(context, selected ? theme.selectedIconColour : theme.unselectedIconColour);
            if (selected && isLoading)
                content.setViewVisibility(theme == Theme.DARK ? progressSelectedDarkThemeId : progressSelectedLightThemeId, View.VISIBLE);
        }

        // The colour filter currently ignores alpha, but we're resetting the alpha to 1.0 anyway just in case this behaviour changes a future version of the OS
        content.setInt(buttonId, "setColorFilter", ColorUtils.setAlphaComponent(iconColour, 0xFF));

        // In API 15, the method is called "setAlpha", but it was deprecated in API 16
        content.setInt(buttonId, Build.VERSION.SDK_INT >= 16 ? "setImageAlpha" : "setAlpha", Color.alpha(iconColour));

        content.setOnClickPendingIntent(buttonId, PendingIntent.getBroadcast(context, pendingIntentCode, onClickIntent, PendingIntentIds.PENDING_INTENT_FLAGS));
        content.setContentDescription(buttonId, contentDescription);

        return this;
    }

    @ColorInt
    private int getButtonBackgroundColour(boolean isButtonSelected) {
        if (!isButtonSelected)
            return Color.TRANSPARENT;
        else if (isColour) {
            // Using primary colour as the background to make the selection stand out
            return buttonColour;
        } else // Selected
            return ContextCompat.getColor(context, theme.selectedBackgroundColour);
    }

    /**
     * Pass this into the notification builder.
     */
    public RemoteViews getContentView() {
        return content;
    }

    private enum Theme {

        DARK(
                R.color.notification_button_background_selected_dark_theme,
                R.color.notification_button_icon_selected_dark_theme,
                R.color.notification_button_icon_unselected_dark_theme,
                R.color.notification_button_icon_on_colour_dark_theme
        ),
        LIGHT(
                R.color.notification_button_background_selected_light_theme,
                R.color.notification_button_icon_selected_light_theme,
                R.color.notification_button_icon_unselected_light_theme,
                R.color.notification_button_icon_on_colour_light_theme
        );

        /**
         * Background colour for selected button when not using coloured background
         */
        @ColorRes
        public final int selectedBackgroundColour;

        /**
         * Icon colour for selected button when not using coloured background
         */
        @ColorRes
        public final int selectedIconColour;

        /**
         * Icon colour for unselected button
         */
        @ColorRes
        public final int unselectedIconColour;

        /**
         * Icon colour for button with coloured background
         */
        @ColorRes
        public final int colourOnButtonColour;

        Theme(
                @ColorRes int selectedBackgroundColour,
                @ColorRes int selectedIconColour,
                @ColorRes int unselectedIconColour,
                @ColorRes int colourOnButtonColour
        ) {
            this.selectedBackgroundColour = selectedBackgroundColour;
            this.selectedIconColour = selectedIconColour;
            this.unselectedIconColour = unselectedIconColour;
            this.colourOnButtonColour = colourOnButtonColour;
        }
    }

}
