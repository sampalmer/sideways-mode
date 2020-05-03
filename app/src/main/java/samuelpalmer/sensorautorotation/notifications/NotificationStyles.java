package samuelpalmer.sensorautorotation.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.InvocationTargetException;

import samuelpalmer.sensorautorotation.R;

public final class NotificationStyles {

    private final StyleType title = new StyleType("TITLE");
    private final StyleType text = new StyleType("SUBTITLE");
    private final StyleType actionButton = new StyleType("ACTION");
    private final Context context;
    private final int exampleIconResource;

    /**
     * You might want to cache the result for performance & memory.
     */
    public NotificationStyles(Context context, int exampleActionIconResource) {
        this.context = context;
        this.exampleIconResource = exampleActionIconResource;
        extractColors();
    }

    /**
     * Gets the colour of the title from {@link NotificationCompat.Builder#setContentTitle(CharSequence)}
     */
    @ColorInt
    public int getTitleColour() {
        return title.colour;
    }

    @ColorInt
    public int getActionButtonColour() {
        int result;

        if (actionButton.colour != null)
            result = actionButton.colour;
        else { // Android 4.0 doesn't have notification actions
            if (title.colour != null)
                result = Color.rgb(Color.red(title.colour), Color.green(title.colour), Color.blue(title.colour));
            else
                result = Color.BLACK;

            result = Color.argb((int) (255 * .8), Color.red(result), Color.green(result), Color.blue(result));
        }

        return result;
    }

    private void extractColors() {
        // Build
        RemoteViews contentView = getContentView();

        LinearLayout group = new LinearLayout(context);
        ViewGroup event = (ViewGroup) contentView.apply(context, group);
        extractColours(event);
        group.removeAllViews();

        if (title.colour == null)
            Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification title colour");
        if (text.colour == null)
            Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification text colour");
        if (VERSION.SDK_INT >= 16 && actionButton.colour == null)
            Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification action colour");
    }

    private RemoteViews getContentView() {
        // Using a separate builder to determine whether a big content view is available
        // Not using the actual builder in case we accidentally influence its output
        Notification.Builder dummyBuilder = createBuilder();
        boolean hasBigContentView = createBigContentView(dummyBuilder) != null;

        // Putting a custom layout into whichever view is *not* being used to coerce Samsung devices
        // running Android 10 into thinking it's a custom notification. This is needed to prevent
        // us from getting stock colours and then using them in a custom notification, which will
        // get different treatment by the OS.
        RemoteViews dummyLayout = new RemoteViews(context.getPackageName(), R.layout.notification_dummy);
        Notification.Builder builder = createBuilder();

        if (hasBigContentView) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.setCustomContentView(dummyLayout);
            else
                builder.setContent(dummyLayout);

            return createBigContentView(builder);
        } else {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.setCustomBigContentView(dummyLayout);

            return createContentView(builder);
        }
    }

    private static RemoteViews createBigContentView(Notification.Builder builder) {
        return VERSION.SDK_INT >= Build.VERSION_CODES.N ? builder.createBigContentView() : builder.build().bigContentView;
    }

    private static RemoteViews createContentView(Notification.Builder builder) {
        return VERSION.SDK_INT >= Build.VERSION_CODES.N ? builder.createContentView() : builder.build().contentView;
    }

    private Notification.Builder createBuilder() {
        // Not using NotificationCompat here because it doesn't give us access to the notification RemoteView
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title.searchTip)
                .setContentText(text.searchTip);

        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            // Setting our usual colour because Android N+ use the colour on the action buttons.
            builder
                    .setColor(NotificationBuilderFactory.getNotificationColour(context))
                    .setVisibility(Notification.VISIBILITY_PUBLIC);

        // Add action button
        String actionTitle = actionButton.searchTip;
        PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent("").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
        builder.addAction(exampleIconResource, actionTitle, intent);

        return builder;
    }

    private void extractColours(ViewGroup viewGroup)
    {
        for (int i = 0; i < viewGroup.getChildCount(); ++i)
        {
            View view = viewGroup.getChildAt(i);
            
            if (view instanceof TextView)
            {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();

                if (text.equals(title.searchTip))
                    title.colour = extractTextColour(textView);

                if (text.equals(this.text.searchTip))
                    this.text.colour = extractTextColour(textView);

                if (text.equals(actionButton.searchTip))
                    actionButton.colour = extractActionColour(textView);
            }
            else if (view instanceof ViewGroup)
                extractColours((ViewGroup) view);
        }
    }

    @ColorInt
    private Integer extractActionColour(TextView textView) {
        if (Build.VERSION.SDK_INT < 21)
            // These versions of Android didn't recolour the icon, so it should be the same as the source image, which should be white.
            return Color.WHITE;
        
        Drawable[] drawables = textView.getCompoundDrawables();
        if (drawables.length > 0) {
            Drawable actionIcon = drawables[0];
            if (actionIcon != null) {
                ColorFilter colorFilter = actionIcon.getColorFilter();
                if (colorFilter instanceof PorterDuffColorFilter)
                    try {
                        return (int) colorFilter.getClass().getMethod("getColor").invoke(colorFilter);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException ignored) {
                    }
            }
        }
        
        // We didn't find the action icon colour, but the text colour should be a good enough approximation
        return extractTextColour(textView);
    }

    @ColorInt
    private int extractTextColour(TextView textView) {
        return textView.getTextColors().getDefaultColor();
    }

    private static class StyleType {
        @ColorInt
        private Integer colour;
        private final String searchTip;

        private StyleType(String tip) {
            searchTip = tip;
        }
    }
}
