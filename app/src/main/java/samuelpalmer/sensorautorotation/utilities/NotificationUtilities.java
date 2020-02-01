package samuelpalmer.sensorautorotation.utilities;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.processes.service.ServiceNotification;

public class NotificationUtilities {

    private NotificationUtilities(){}

    /**
     * Fetches the id of {@code notification_main_column} in Android's standard notification
     * template.
     *
     * @return The id or {@link View#NO_ID} if not available
     */
    @IdRes
    public static int getMainContentId(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return View.NO_ID;

        RemoteViews notificationContent = NotificationBuilderFactory.create(context)
                .setSmallIcon(R.drawable.app_right_side)
                .setCustomContentView(new RemoteViews(context.getPackageName(), R.layout.notification_marker))
                .setStyle(new Notification.DecoratedCustomViewStyle())
                .createContentView();

        FrameLayout host = new FrameLayout(context);
        View inflated = notificationContent.apply(context, host);
        View customContent = inflated.findViewById(R.id.notification_marker);
        ViewParent customContentParent = customContent.getParent();
        if (customContentParent == null) {
            Log.w(ServiceNotification.class.getSimpleName(), "Notification custom content has no parent");
            return View.NO_ID;
        }

        if (!(customContentParent instanceof View)) {
            Log.w(ServiceNotification.class.getSimpleName(), "Notification cusotm content parent is a " + customContentParent.getClass().getName());
            return View.NO_ID;
        }

        View mainContent = (View) customContentParent;
        return mainContent.getId();
    }

}
