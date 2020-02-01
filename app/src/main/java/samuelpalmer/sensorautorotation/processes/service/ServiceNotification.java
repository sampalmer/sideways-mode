package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationButtons;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationStyles;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceNotification {
    
    private final Context context;
    private final NotificationBuilderCompat builder;
    
    // Cached for RAM & CPU efficiency
    @ColorInt
    private final int titleColour;

    // Cached for RAM & CPU efficiency
    @ColorInt
    private final int actionButtonColour;

    public ServiceNotification(Context context) {
        if (!CurrentProcess.isServiceProcess(context))
            throw new RuntimeException("Cannot use service notification from outside of service process");

        this.context = context;
        
        PendingIntent dashboardIntent = PendingIntent.getActivity(
        this.context,
            PendingIntentIds.MAIN_NOTIFICATION_CLICK,
            new Intent(this.context, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntentIds.PENDING_INTENT_FLAGS);
        
        // Caching and re-using the builder prevents the notification from disappearing and
        // reappearing each time it is updated on my Sony Xperia M (Android 4.3).
        builder = NotificationBuilderFactory.create(this.context)
            .setContentIntent(dashboardIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // So when the user unlocks the phone, they can see if they've left the app on
            .setChannelId(NotificationChannels.ID_CONTROLS)
            .setOnlyAlertOnce(true) // Don't want to alert the user each time we update the state of the service notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true) // Prevents the user from dismissing the notification when the service is stopped
            .setVibrate(null) // Just to make sure this never triggers a vibration, even if the user configures it to do so, since the notification is a response to user interaction
            .setSound(null) // Just to make sure this never triggers a sound, even if the user configures it to do so, since the notification is a response to user interaction
        ;

        hideTimestamp();
        
        NotificationStyles styles = new NotificationStyles(this.context, R.drawable.action_turn_off);
        titleColour = styles.getTitleColour();
        actionButtonColour = styles.getActionButtonColour();

        Log.i(ServiceNotification.class.getSimpleName(), "Text colour: " + Integer.toHexString(titleColour));
        Log.i(ServiceNotification.class.getSimpleName(), "Action button colour: " + Integer.toHexString(actionButtonColour));
    }

    /**
     * @param runningState set to {@code null} if the app isn't running (and the user is hence upright)
     */
    public Notification build(@Nullable ApplicationStatus.RunningState runningState) {
        builder.setSmallIcon(runningState == null ? R.drawable.app_upright_filled : runningState.userSide.notificationIconId);

        int statusTextResource;

        if (runningState == null)
            statusTextResource = R.string.user_rotation_upright;
        else if (runningState.waitingForSensor)
            statusTextResource = R.string.status_waiting_sensor;
        else
            statusTextResource = runningState.userSide.descriptionId;
        
        CharSequence statusText = context.getText(statusTextResource);

        if (VERSION.SDK_INT >= 24) {
            // In Android N, the app name is already shown in the notification's header section, so we can skip it here.
            // Plus, we can now make the notification thin, so we'll put the status to the right of the app name to save vertical space
            builder.setSubText(statusText);
        }
        else {
            builder.setContentTitle(context.getText(R.string.app_name_full));
            builder.setContentText(statusText);
        }
        
        modifyContentView(runningState);

        Notification notification = builder.build();

        // The OS sets these flags when the notification is attached to a foreground service,
        // so we're also adding them here to ensure we get the same behaviour when the service isn't running.
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // Not adding FLAG_FOREGROUND_SERVICE since that makes it impossible to remove the
        // notification with .cancel() when the user turns off the "keep notification" option

        return notification;
    }

    /**
     * @param runningState set to {@code null} if the app isn't running (and the user is hence upright)
     */
    private void modifyContentView(@Nullable ApplicationStatus.RunningState runningState) {
        @Nullable UserSide userSide = runningState == null ? null : runningState.userSide;

        builder.setCustomContentView(null); // So the pre-buttons notifification doesn't contain the buttons from last time around.

        NotificationButtons notificationButtons = new NotificationButtons(
            context,
            R.layout.notification,
            builder.createContentView(),
            titleColour,
            actionButtonColour
        );

        boolean isLoading = runningState != null && runningState.waitingForSensor;

        notificationButtons
            .configureButton(
                R.id.notification_left_side_button_container,
                R.id.notification_left_side_button,
                R.id.notification_left_side_progress,
                R.drawable.app_left_side,
                PendingIntentIds.MAIN_NOTIFICATION_LEFT_SIDE,
                // Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
                userSide != null && userSide.equals(UserSide.LEFT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_LEFT_SIDE).asIntent(context),
                context.getText(R.string.user_rotation_left_side),
                UserSide.LEFT.equals(userSide),
                isLoading && runningState.userSide.equals(UserSide.LEFT))
            .configureButton(
                R.id.notification_turn_off_button_container,
                R.id.notification_turn_off_button,
                R.id.notification_turn_off_progress,
                R.drawable.action_turn_off,
                PendingIntentIds.MAIN_NOTIFICATION_TURN_OFF,
                new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).asIntent(context),
                context.getText(R.string.action_turn_off),
                userSide == null,
                false)
            .configureButton(
                R.id.notification_right_side_button_container,
                R.id.notification_right_side_button,
                R.id.notification_right_side_progress,
                R.drawable.app_right_side,
                PendingIntentIds.MAIN_NOTIFICATION_RIGHT_SIDE,
                // Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
                userSide != null && userSide.equals(UserSide.RIGHT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_RIGHT_SIDE).asIntent(context),
                context.getText(R.string.user_rotation_right_side),
                UserSide.RIGHT.equals(userSide),
                isLoading && runningState.userSide.equals(UserSide.RIGHT))
        ;

        builder.setCustomContentView(notificationButtons.getContentView());
    }

    private void hideTimestamp() {
        if (Build.VERSION.SDK_INT >= 17)
            builder.setShowWhen(false);
        else
            builder.setWhen(0);
    }

}
