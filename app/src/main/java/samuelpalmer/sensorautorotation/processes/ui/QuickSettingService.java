package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
    
    private static final String TAG = QuickSettingService.class.getSimpleName();
    
    private ApplicationStatusClient statusClient;
    private ApplicationStatus status;
    private boolean pendingClick;
    private boolean isListening;
    
    @Override
    public void onCreate() {
        statusClient = new ApplicationStatusClient(this, statusListener, Looper.myLooper());
    }
    
    /**
     * Doesn't seem to fire when the tile is in STATE_UNAVAILABLE
     */
    @Override
    public void onClick() {
        super.onClick();

        Log.i(TAG, "Clicked");
        
        if (status != null) {
            handleClick();
            pendingClick = false;
        }
        else {
            Log.i(TAG, "Don't know application status to handle click. Waiting until status is available.");
            pendingClick = true;
        }
    }
    
    /**
     * Assumes that we have the current status.
     */
    private void handleClick() {
        if (status.runningState != null)
            new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(this);
        else
            // Launching the dashboard regardless of whether we are on the lock screen since the user might want to start the app before unlocking their device
            launchDashboard();
    }
    
    private void launchDashboard() {
        /*
         * Not using the quick setting dialog because:
         *  1. It is affected by an OS layout bug (https://issuetracker.google.com/issues/38188882)
         *  2. It appears over system activities such as "recents"
         *  3. Keeping a separate dashboard dialog and dashboard activity in sync is messy
         *  4. It occasionally crashes
         */
        
        Intent intent = new Intent(this, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PendingIntentIds.QUICK_SETTING_CLICK, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
        
        if (isLocked())
            // Not collapsing the status bar if the device is locked because then the keyguard pops up if it's the first time the user has opened the app
            startActivity(intent);
        else
            // This is needed to collapse the status bar if the device is unlocked
            startActivityAndCollapse(intent);
        
        // Using a PendingIntent works around the issue where pressing the home button causes a delay in activity starts (https://issuetracker.google.com/issues/36910222)
        // Note that this no longer works in Android 10
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void onTileAdded() {
        super.onTileAdded();

        Log.i(TAG, "Tile added");
    }
    
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();

        Log.i(TAG, "Tile removed");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        isListening = true;
        statusClient.subscribe();
        status = null; // We must have been previously unsubscribed, so any status we had was out-of-date.
        
        /*
        Not updating the tile here because
        1. We might not have a status
        2. Even if we have a status, it might be out-of-date
        */
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        isListening = false;
        statusClient.unsubscribe();
        status = null;
    }
    
    private final ApplicationStatusClient.Listener statusListener = new ApplicationStatusClient.Listener() {
        @Override
        public void onChange(ApplicationStatus status) {
            if (isListening) {
                QuickSettingService.this.status = status;
                
                if (status != null) {
                    updateTile();
                    
                    if (pendingClick) {
                        Log.i(TAG, "Got status update. Handling pending click.");
                        handleClick();
                        pendingClick = false;
                    }
                }
            }
        }
    };
    
    /**
     * Assumes {@link #status} is available.
     */
    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            /*
            This happened once on the Android 7.1.1 emulator even though the tile was in listening state.
            Maybe the OS isn't guaranteed to have the tile available during listening state.
            Or maybe it's just an Android bug.
            */
            Log.w(TAG, "Couldn't get quick setting tile. Maybe the service isn't in listening state?");
            return;
        }
        
        if (status.runningState != null)
            tile.setState(Tile.STATE_ACTIVE);
        else
            tile.setState(Tile.STATE_INACTIVE);
        
        tile.setIcon(Icon.createWithResource(this, status.runningState != null ? status.runningState.userSide.quickSettingIconId : UserSide.RIGHT.quickSettingIconId));

        if (Build.VERSION.SDK_INT >= 29) {
            // Android Q lets you specify the subtitle, so we'll use that to show the status
            tile.setLabel(getText(R.string.app_name_full));

            if (status.runningState == null) {
                // Not showing the state if the app is turned off. This is consistent with the other OS quick setting tiles.
                tile.setSubtitle(null);
            } else {
                int subtitleId =  status.runningState.userSide.descriptionId;
                tile.setSubtitle(getText(subtitleId));
            }
        }
        else {
            // Older versions of Android only let you specify the label, so we'll have to make do with that
            int labelId = status.runningState != null ? status.runningState.userSide.descriptionId : R.string.app_name_full;
            tile.setLabel(getText(labelId));
        }

        if (Build.VERSION.SDK_INT >= 30) {
            int stateDescriptionId = status.runningState != null ? status.runningState.userSide.descriptionId : R.string.user_rotation_off;
            tile.setStateDescription(getText(stateDescriptionId));
        }

        tile.updateTile(); // Seems to subsequently cause loss of listening state.
    }

}
