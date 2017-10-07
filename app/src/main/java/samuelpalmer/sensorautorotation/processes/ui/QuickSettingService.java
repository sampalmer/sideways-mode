package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingService extends TileService {
	
	private ApplicationStatusClient statusClient;
	private ApplicationStatus status;
	private boolean pendingClick;
	
	/**
	 * Doesn't seem to fire when the tile is in STATE_UNAVAILABLE
	 */
	@Override
	public void onClick() {
		super.onClick();

		Log.i(QuickSettingService.class.getSimpleName(), "Clicked");
		
		if (status != null) {
			handleClick();
			pendingClick = false;
		}
		else {
			Log.i(QuickSettingService.class.getSimpleName(), "Don't know application status to handle click. Waiting until status is available.");
			pendingClick = true;
		}
	}
	
	/**
	 * Assumes that we have the current status.
	 */
	private void handleClick() {
		if (status.appEnabled)
			new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(this);
		else if (isLocked())
			// On a non-secure lock screen, we can smoothly unlock the device and open the dashboard.
			unlockAndRun(new Runnable() {
				@Override
				public void run() {
					launchDashboard();
				}
			});
		else {
			launchDashboard();
		}
	}
	
	private void launchDashboard() {
		/*
		 * Not using the quick setting dialog because:
		 *  1. It is affected by an OS layout bug (https://issuetracker.google.com/issues/38188882)
		 *  2. It appears over system activities such as "recents"
		 *  3. It's simpler to just use the one activity for all possible ways of accessing the dashboard
		 */
		
		Intent intent = new Intent(this, DashboardActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, PendingIntentIds.QUICK_SETTING_CLICK, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
		
		// This is needed to collapse the status bar.
		startActivityAndCollapse(intent);
		
		// Using a PendingIntent works around the issue where pressing the home button causes a delay in activity starts (https://issuetracker.google.com/issues/36910222)
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onTileAdded() {
		super.onTileAdded();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile added");
	}
	
	@Override
	public void onTileRemoved() {
		super.onTileRemoved();

		Log.i(QuickSettingService.class.getSimpleName(), "Tile removed");
	}

	@Override
	public void onStartListening() {
		super.onStartListening();
		
		boolean boundSuccessfully = bindService(
			new Intent(this, ApplicationStatusService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
		
		if (!boundSuccessfully) {
			// The documentation says you should still unbind after a binding failure
			unbindService(serviceConnection);
			throw new RuntimeException("Couldn't connect to " + ApplicationStatusService.class.getSimpleName());
		}
	}

	@Override
	public void onStopListening() {
		super.onStopListening();
		
		if (statusClient != null) {
			try {
				statusClient.unsubscribe();
			} catch (RemoteException ignored) {
				// The service process died, but that doesn't matter since we're unsubscribing.
			}
			
			disconnected();
		}
		
		unbindService(serviceConnection);
	}
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			statusClient = new ApplicationStatusClient(iBinder, statusListener);
			
			try {
				statusClient.subscribe();
			} catch (RemoteException e) {
				// Service process died, so we'll have to try again once it's back up.
				disconnected();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			disconnected();
		}
	};
	
	private final ApplicationStatusClient.Listener statusListener = new ApplicationStatusClient.Listener() {
		@Override
		public void onChange(ApplicationStatus status) {
			QuickSettingService.this.status = status;
			
			updateTile();
			
			if (pendingClick) {
				Log.i(QuickSettingService.class.getSimpleName(), "Got status update. Handling pending click.");
				handleClick();
				pendingClick = false;
			}
		}
	};
	
	private void disconnected() {
		statusClient = null;
		status = null;
	}
	
	/**
	 * Assumes status is available.
	 */
	private void updateTile() {
		Tile tile = getQsTile();
		if (tile == null)
			throw new RuntimeException("Couldn't get quick setting tile. Maybe the service isn't in listening state?");
		
		if (status.appEnabled)
			tile.setState(Tile.STATE_ACTIVE);
		else
			tile.setState(Tile.STATE_INACTIVE);
		
		//noinspection ResourceType
		tile.setIcon(Icon.createWithResource(this, status.appEnabled ? status.user.iconId : R.drawable.quick_setting_off));
		
		int id = status.appEnabled ? status.user.descriptionId : R.string.app_name;
		tile.setLabel(CurrentProcess.context().getText(id));
		tile.updateTile(); //Seems to subsequently cause loss of listening state.
	}

}
