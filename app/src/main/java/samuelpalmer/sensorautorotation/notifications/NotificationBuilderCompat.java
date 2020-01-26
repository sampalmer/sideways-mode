package samuelpalmer.sensorautorotation.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.widget.RemoteViews;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;

/**
 * A reinvented clone of NotificationCompat.Builder that fixes the following issues:
 * - setShowWhen() not working on API 17 & 18 (https://issuetracker.google.com/issues/37048073)
 * - createContentView() and createBigContentView() missing (https://issuetracker.google.com/issues/37609039)
 */
@SuppressWarnings({"UnusedReturnValue", "ClassWithTooManyMethods", "WeakerAccess"})
public class NotificationBuilderCompat {

    public static NotificationBuilderCompat create(Context context) {
        NotificationBuilderCompat result;

        if (Build.VERSION.SDK_INT >= 26)
            result = new V26();
        else if (VERSION.SDK_INT >= 24)
            result = new V24();
        else if (Build.VERSION.SDK_INT >= 23)
            result = new V23();
        else if (Build.VERSION.SDK_INT >= 21)
            result = new V21();
        else if (Build.VERSION.SDK_INT >= 17)
            result = new V17();
        else if (Build.VERSION.SDK_INT >= 16)
            result = new V16();
        else
            result = new NotificationBuilderCompat();
        
        result.setContext(context);
        
        return result;
    }
    
    protected Context context;
    protected Notification.Builder builder;
    
    protected void setContext(Context context) {
        this.context = context;
        
        //noinspection deprecation
        this.builder = new Notification.Builder(context);
    }

    /**
     * See {@link NotificationCompat.Builder#setSmallIcon(int)}
     */
    public NotificationBuilderCompat setSmallIcon(int icon) {
        builder.setSmallIcon(icon);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setContentIntent(PendingIntent)}
     */
    public NotificationBuilderCompat setContentIntent(PendingIntent intent) {
        builder.setContentIntent(intent);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setContentTitle(CharSequence)}
     */
    public NotificationBuilderCompat setContentTitle(CharSequence title) {
        builder.setContentTitle(title);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setContentText(CharSequence)}
     */
    public NotificationBuilderCompat setContentText(CharSequence text) {
        builder.setContentText(text);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setSubText(CharSequence)}
     */
    public NotificationBuilderCompat setSubText(CharSequence text) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setCategory(String)}
     */
    public NotificationBuilderCompat setCategory(String category) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setColor(int)}
     */
    public NotificationBuilderCompat setColor(@ColorInt int argb) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setVisibility(int)}
     */
    public NotificationBuilderCompat setVisibility(int visibility) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setWhen(long)}
     */
    public NotificationBuilderCompat setWhen(long when) {
        builder.setWhen(when);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#build()}
     */
    public Notification build() {
        //noinspection deprecation
        return builder.getNotification();
    }

    /**
     * See {@link NotificationCompat.Builder#setOnlyAlertOnce(boolean)}
     */
    public NotificationBuilderCompat setOnlyAlertOnce(boolean onlyAlertOnce) {
        builder.setOnlyAlertOnce(onlyAlertOnce);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setPriority(int)}
     */
    public NotificationBuilderCompat setPriority(int pri) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setShowWhen(boolean)}
     */
    public NotificationBuilderCompat setShowWhen(boolean show) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setAutoCancel(boolean)}
     */
    public NotificationBuilderCompat setAutoCancel(boolean autoCancel) {
        builder.setAutoCancel(autoCancel);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setDeleteIntent(PendingIntent)}
     */
    public NotificationBuilderCompat setDeleteIntent(PendingIntent intent) {
        builder.setDeleteIntent(intent);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setTicker(CharSequence)}
     */
    public NotificationBuilderCompat setTicker(CharSequence tickerText) {
        builder.setTicker(tickerText);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setVibrate(long[])}
     */
    public NotificationBuilderCompat setVibrate(long[] pattern) {
        //noinspection deprecation
        builder.setVibrate(pattern);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#addAction(int, CharSequence, PendingIntent)}
     */
    public NotificationBuilderCompat addAction(int icon, CharSequence title, PendingIntent intent) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setCustomContentView(RemoteViews)}
     */
    public NotificationBuilderCompat setCustomContentView(RemoteViews view) {
        //noinspection deprecation
        builder.setContent(view);
        return this;
    }

    /**
     * See {@link Notification.Builder#createBigContentView()}
     */
    public RemoteViews createBigContentView() {
        return null;
    }

    /**
     * See {@link Notification.Builder#createContentView()}
     */
    public RemoteViews createContentView() {
        //noinspection deprecation
        return build().contentView;
    }

    /**
     * See {@link NotificationCompat.Builder#setProgress(int, int, boolean)}
     */
    public NotificationBuilderCompat setProgress(int max, int progress, boolean indeterminate) {
        builder.setProgress(max, progress, indeterminate);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setChannelId(String)}
     */
    public NotificationBuilderCompat setChannelId(String channelId) {
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setOngoing(boolean)}
     */
    public NotificationBuilderCompat setOngoing(boolean ongoing) {
        builder.setOngoing(ongoing);
        return this;
    }

    /**
     * See {@link NotificationCompat.Builder#setSound(Uri)}
     */
    public NotificationBuilderCompat setSound(Uri sound) {
        builder.setSound(sound);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static class V16 extends NotificationBuilderCompat
    {
        @Override
        public Notification build() {
            return builder.build();
        }

        @Override
        public NotificationBuilderCompat setPriority(int pri) {
            //noinspection deprecation
            builder.setPriority(pri);
            return this;
        }

        @Override
        public NotificationBuilderCompat addAction(int icon, CharSequence title, PendingIntent intent) {
            //noinspection deprecation
            builder.addAction(icon, title, intent);
            return this;
        }

        @Override
        public RemoteViews createBigContentView() {
            //noinspection deprecation
            return build().bigContentView;
        }

        @Override
        public NotificationBuilderCompat setSubText(CharSequence text) {
            builder.setSubText(text);
            return this;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static class V17 extends V16
    {
        @Override
        public NotificationBuilderCompat setShowWhen(boolean show) {
            builder.setShowWhen(show);
            return this;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class V21 extends V17
    {
        @Override
        public NotificationBuilderCompat setCategory(String category) {
            builder.setCategory(category);
            return this;
        }

        @Override
        public NotificationBuilderCompat setColor(int argb) {
            builder.setColor(argb);
            return this;
        }

        @Override
        public NotificationBuilderCompat setVisibility(int visibility) {
            builder.setVisibility(visibility);
            return this;
        }
    }

    @TargetApi(VERSION_CODES.M)
    private static class V23 extends V21
    {
        @Override
        public NotificationBuilderCompat addAction(int icon, CharSequence title, PendingIntent intent) {
            builder.addAction(new Action.Builder(Icon.createWithResource(context, icon), title, intent).build());
            return this;
        }
    }

    @TargetApi(VERSION_CODES.N)
    private static class V24 extends V23
    {
        @Override
        public NotificationBuilderCompat setCustomContentView(RemoteViews contentView) {
            builder.setCustomContentView(contentView);
            return this;
        }

        @Override
        public RemoteViews createBigContentView() {
            return builder.createBigContentView();
        }

        @Override
        public RemoteViews createContentView() {
            return builder.createContentView();
        }
    }

    @TargetApi(VERSION_CODES.O)
    private static class V26 extends V24
    {
        @Override
        protected void setContext(Context context) {
            this.context = context;
            this.builder = new Notification.Builder(context, null);
        }
        
        @Override
        public NotificationBuilderCompat setVibrate(long[] pattern) {
            return this;
        }
        
        @Override
        public NotificationBuilderCompat setPriority(int pri) {
            return this;
        }
        
        @Override
        public NotificationBuilderCompat setChannelId(String channelId) {
            builder.setChannelId(channelId);
            return this;
        }

        @Override
        public NotificationBuilderCompat setSound(Uri sound) {
            return this;
        }
    }
}
