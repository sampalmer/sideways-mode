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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplay;

/**
 * Provides access to remotely monitor the state of the service.
 * Not using a content provider for this since they have various issues (randomly returning null, provider can't even be found in rare situations, poor error handling, blocking I/O)
 */
public class ApplicationStatusService extends Service {
	
	private static final Set<Messenger> clients = new HashSet<>();
	
	private static Settings.Setting<UserSide> enabledState = ServiceSettings.instance().persistentState();
	private static Settings.Setting<String> error = ServiceSettings.instance().lastErrorJson();
	
	private static final int MSG_SUBSCRIBE = 1;
	private static final int MSG_UNSUBSCRIBE = 2;
	
	private static class ServerHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
				case MSG_SUBSCRIBE: {
					final Messenger newClient = msg.replyTo;
					boolean isNewSubscription = clients.add(newClient);
					
					if (isNewSubscription) {
						Message message = null;
						
						if (enabledState.hasValue())
							if (ApplicationInstanceManager.isApplicationRunning()) {
								//TODO: Replace all this with an EventRegistrarAdaptor and ApplicationStatusDisplays.add()
								ApplicationStatus lastStatus = ApplicationInstanceManager.applicationInstance().applicationStatusDisplayUpdater.getLastStatus();
								if (lastStatus != null)
									message = lastStatus.asMessage();
							} else {
								/*
								 * If the service has crashed or been killed, we want to make sure it's running.
								 * Otherwise the client will have to wait a long time (or forever) to get the first service status update.
								 */
								ServiceController.updateServiceState(true);
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
							
						if (clients.size() == 1) {
							enabledState.subscribe(enabledStateChanged);
							error.subscribe(enabledStateChanged);
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
					else if (clients.size() == 0) {
						error.unsubscribe(enabledStateChanged);
						enabledState.unsubscribe(enabledStateChanged);
					}
					
					break;
				}
				default:
					throw new RuntimeException("Received unknown message: " + msg.what);
			}
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
			if (!enabledState.hasValue())
				sendUpdate(makeAppDisabledStatus());
		}
	};
	
	public static class Client {
		
		private final Messenger serviceMessenger;
		private final Listener listener;
		private final Messenger clientMessenger;
		
		public Client(IBinder service, Listener listener) {
			this.serviceMessenger = new Messenger(service);
			this.listener = listener;
			this.clientMessenger = new Messenger(new ClientHandler(this));
		}
		
		public void subscribe() throws RemoteException {
			Message msg = Message.obtain(null, MSG_SUBSCRIBE);
			msg.replyTo = clientMessenger;
			serviceMessenger.send(msg);
		}
		
		public void unsubscribe() throws RemoteException {
			Message msg = Message.obtain(null, MSG_UNSUBSCRIBE);
			msg.replyTo = clientMessenger;
			serviceMessenger.send(msg);
		}
		
		public interface Listener {
			void onChange(ApplicationStatus status);
		}
		
		private static class ClientHandler extends Handler {
			
			private final WeakReference<Client> clientReference;
			
			public ClientHandler(Client client) {
				// Android Studio issued a warning saying that I must use weak references to outside of the handler, hence the use of weak references here
				this.clientReference = new WeakReference<>(client);
			}
			
			@Override
			public void handleMessage(Message msg) {
				ApplicationStatus status = new ApplicationStatus(msg);
				Client client = clientReference.get();
				if (client != null)
					client.listener.onChange(status);
				else {
					// Can't do much here
					Log.w(getClass().getSimpleName(), "Received client message but client has been garbage collected");
				}
			}
			
		}
		
	}

	public static class RemoteStatusDisplay implements ApplicationStatusDisplay {

		@Override
		public void onApplicationStopped() {}

		@Override
		public void update(ApplicationStatus applicationStatus) {
			sendUpdate(applicationStatus);
		}

	}
	
	private static void sendUpdate(ApplicationStatus applicationStatus) {
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
	}
	
}
