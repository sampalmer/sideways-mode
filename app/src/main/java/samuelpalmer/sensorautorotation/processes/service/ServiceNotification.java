package samuelpalmer.sensorautorotation.processes.service;

import static samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory.getNotificationColour;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.CustomNotificationTemplate;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceNotification {
    
    private final Context context;
    private final NotificationCompat.Builder builder;
    
    public ServiceNotification(Context context) {
        if (!CurrentProcess.isServiceProcess())
            throw new RuntimeException("Cannot use service notification from outside of service process");

        this.context = context;

        PendingIntent dashboardIntent = PendingIntent.getActivity(
            this.context,
            PendingIntentIds.MAIN_NOTIFICATION_CLICK,
            new Intent(this.context, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntentIds.PENDING_INTENT_FLAGS);

        // Caching and re-using the builder prevents the notification from disappearing and
        // reappearing each time it is updated on my Sony Xperia M (Android 4.3).
        builder = new NotificationCompat.Builder(context, NotificationChannels.ID_CONTROLS)
                .setColor(getNotificationColour(context))
                .setContentIntent(dashboardIntent)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // So when the user unlocks the phone, they can see if they've left the app on
                .setOnlyAlertOnce(true) // Don't want to alert the user each time we update the state of the service notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true) // Prevents the user from dismissing the notification when the service is stopped
                .setVibrate(null) // Just to make sure this never triggers a vibration, even if the user configures it to do so, since the notification is a response to user interaction
                .setSound(null) // Just to make sure this never triggers a sound, even if the user configures it to do so, since the notification is a response to user interaction
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // Prevents Android 12 and above from delaying display of the service notification
        ;
    }

    /**
     * @param runningState set to {@code null} if the app isn't running (and the user is hence upright)
     */
    public Notification build(@Nullable ApplicationStatus.RunningState runningState) {
        builder.setSmallIcon(runningState == null ? R.drawable.app_upright_filled : runningState.userSide.notificationIconId);
        builder.setContentTitle(makeStatusText(runningState));

        modifyContentView(runningState);

        Notification notification = builder.build();

        // The OS sets these flags when the notification is attached to a foreground service,
        // so we're also adding them here to ensure we get the same behaviour when the service isn't running.
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // Not adding FLAG_FOREGROUND_SERVICE since that makes it impossible to remove the
        // notification with .cancel() when the user turns off the "keep notification" option

        return notification;
    }

    private CharSequence makeStatusText(@Nullable ApplicationStatus.RunningState runningState) {
        int statusTextResource;

        if (runningState == null)
            statusTextResource = R.string.user_rotation_upright;
        else if (runningState.waitingForSensor)
            statusTextResource = R.string.status_waiting_sensor;
        else
            statusTextResource = runningState.userSide.descriptionId;

        return context.getText(statusTextResource);
    }

    /**
     * @param runningState set to {@code null} if the app isn't running (and the user is hence upright)
     */
    private void modifyContentView(@Nullable ApplicationStatus.RunningState runningState) {
        @Nullable UserSide userSide = runningState == null ? null : runningState.userSide;

        CustomNotificationTemplate template = new CustomNotificationTemplate(
            context,
            R.layout.notification
        );

        boolean isLoading = runningState != null && runningState.waitingForSensor;

        template
            .configureButton(
                R.id.notification_left_side_button_container,
                R.id.notification_left_side_icon_unselected,
                R.id.notification_left_side_icon_selected,
                R.id.notification_left_side_button,
                R.id.notification_left_side_progress,
                PendingIntentIds.MAIN_NOTIFICATION_LEFT_SIDE,
                // Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
                userSide != null && userSide.equals(UserSide.LEFT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_LEFT_SIDE).asIntent(context),
                UserSide.LEFT.equals(userSide),
                isLoading && runningState.userSide.equals(UserSide.LEFT))
            .configureButton(
                R.id.notification_turn_off_button_container,
                R.id.notification_turn_off_icon_unselected,
                R.id.notification_turn_off_icon_selected,
                R.id.notification_turn_off_button,
                R.id.notification_turn_off_progress,
                PendingIntentIds.MAIN_NOTIFICATION_TURN_OFF,
                new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).asIntent(context),
                userSide == null,
                false)
            .configureButton(
                R.id.notification_right_side_button_container,
                R.id.notification_right_side_icon_unselected,
                R.id.notification_right_side_icon_selected,
                R.id.notification_right_side_button,
                R.id.notification_right_side_progress,
                PendingIntentIds.MAIN_NOTIFICATION_RIGHT_SIDE,
                // Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
                userSide != null && userSide.equals(UserSide.RIGHT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_RIGHT_SIDE).asIntent(context),
                UserSide.RIGHT.equals(userSide),
                isLoading && runningState.userSide.equals(UserSide.RIGHT))
        ;

        // Using a fully-custom view because:
        // * Generating and then tweaking an OS notification doesn't work on MIUI
        // * The built-in MediaStyle template is expandable, has a large icon, and sometimes has a coloured background
        // * The built-in DecoratedCustomViewStyle templates doesn't leave enough vertical space for our buttons
        RemoteViews customNotification = template.getContentView();
        builder.setCustomContentView(customNotification);
    }

}
