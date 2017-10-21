package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.Settings;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

/**
 * Provides access to remotely monitor the state of the service.
 * Not using a content provider for this since they have various issues (randomly returning null, provider can't even be found in rare situations, poor error handling, blocking I/O)
 */
public class ApplicationStatusService extends Service {
	
	private static final Set<Messenger> clients = new HashSet<>();
	
	private static final Settings.Setting<String> error = ServiceSettings.instance().lastErrorJson();
	private static boolean isSubscribed = false;
	
	public static final int MSG_SUBSCRIBE = 1;
	public static final int MSG_UNSUBSCRIBE = 2;
	
	private static class ServerHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
				case MSG_SUBSCRIBE: {
					final Messenger newClient = msg.replyTo;
					boolean isNewSubscription = clients.add(newClient);
					
					if (isNewSubscription) {
						Message message = null;
						
						if (ServiceRunningStateHolder.isServiceRunning()) {
							//TODO: Replace all this with an EventRegistrarAdaptor and ApplicationStatusDisplays.add()
							ApplicationStatus lastStatus = ServiceRunningStateHolder.getServiceInstance().applicationStatusDisplayUpdater.getLastStatus();
							if (lastStatus != null)
								message = lastStatus.asMessage();
						}
						else {
							message = makeAppDisabledStatus().asMessage();
						}
						
						if (message != null)
							try {
								newClient.send(message);
							} catch (RemoteException e) {
								// The client is dead. Remove it from the list.
								clients.remove(newClient);
							}
					}
					else
						Log.w(ApplicationStatusService.class.getSimpleName(), "Received duplicate subscription");
					
					break;
				}
				case MSG_UNSUBSCRIBE: {
					boolean wasSubscribed = clients.remove(msg.replyTo);
					if (!wasSubscribed)
						Log.w(ApplicationStatusService.class.getSimpleName(), "Received unsubscription for non-subscriber");
					
					break;
				}
				default:
					throw new RuntimeException("Received unknown message: " + msg.what);
			}
			
			syncInternalSubscriptions();
		}
		
	}
	
	private static ApplicationStatus makeAppDisabledStatus() {
		return new ApplicationStatus(
			error.hasValue() ? EnvironmentError.fromJson(error.get()) : null,
			false,
			null,
			false
		);
	}
	
	private final Messenger messenger = new Messenger(new ServerHandler());
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		throw new RuntimeException("This service cannot be started; it is only for binding.");
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
	
	private static final EventHandler enabledStateChanged = new EventHandler() {
		@Override
		public void update() {
			if (!ServiceRunningStateHolder.isServiceRunning())
				sendUpdate(makeAppDisabledStatus());
		}
	};
	
	public static void sendUpdate(ApplicationStatus applicationStatus) {
		Message message = applicationStatus.asMessage();
		
		Iterator<Messenger> iterator = clients.iterator();
		while (iterator.hasNext()) {
			Messenger client = iterator.next();
			
			try {
				client.send(message);
			}
			catch (RemoteException e) {
				// The client is dead. Remove it from the list.
				iterator.remove();
			}
		}
		
		syncInternalSubscriptions(); // In case we've removed a client
	}
	
	private static void syncInternalSubscriptions() {
		if (clients.size() > 0 && !isSubscribed) {
			ServiceRunningStateHolder.onStartedOrStopped.subscribe(enabledStateChanged);
			error.subscribe(enabledStateChanged);
			isSubscribed = true;
		}
		else if (clients.size() == 0 && isSubscribed) {
			error.unsubscribe(enabledStateChanged);
			ServiceRunningStateHolder.onStartedOrStopped.unsubscribe(enabledStateChanged);
			isSubscribed = false;
		}
	}
	
}
