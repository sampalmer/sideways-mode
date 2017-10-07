package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;

public class ApplicationStatusClient {
	
	private final Context context;
	private final Listener listener;
	private final Messenger clientMessenger;
	
	private boolean bound;
	private @Nullable Messenger serviceMessenger; // Only available when the service both bound and connected.
	
	public ApplicationStatusClient(Context context, Listener listener) {
		this.context = context;
		this.listener = listener;
		this.clientMessenger = new Messenger(new ClientHandler(this));
	}
	
	public void subscribe() {
		if (bound)
			throw new RuntimeException("Already subscribed");
		
		bound = context.bindService(
			new Intent(context, ApplicationStatusService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
		
		if (!bound) {
			// The documentation says you should still unbind after a binding failure
			context.unbindService(serviceConnection);
			throw new RuntimeException("Couldn't connect to " + ApplicationStatusService.class.getSimpleName());
		}
	}
	
	public void unsubscribe() {
		if (!bound)
			throw new RuntimeException("Not subscribed");
		
		if (serviceMessenger != null) {
			try {
				Message msg = Message.obtain(null, ApplicationStatusService.MSG_UNSUBSCRIBE);
				msg.replyTo = clientMessenger;
				serviceMessenger.send(msg);
			} catch (RemoteException ignored) {
				// The service process died, but that doesn't matter since we're unsubscribing.
			}
			
			disconnected();
		}
		
		context.unbindService(serviceConnection);
		
		bound = false;
	}
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceMessenger = new Messenger(iBinder);
			
			try {
				Message msg = Message.obtain(null, ApplicationStatusService.MSG_SUBSCRIBE);
				msg.replyTo = clientMessenger;
				serviceMessenger.send(msg);
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
	
	private void disconnected() {
		serviceMessenger = null;
		listener.onChange(null);
	}
	
	public interface Listener {
		/**
		 * @param status The application status or {@code null} when the remote connection is disconnected.
		 */
		void onChange(@Nullable ApplicationStatus status);
	}
	
	private static class ClientHandler extends Handler {
		
		private final WeakReference<ApplicationStatusClient> clientReference;
		
		public ClientHandler(ApplicationStatusClient client) {
			// Android Studio issued a warning saying that I must use weak references to outside of the handler, hence the use of weak references here
			this.clientReference = new WeakReference<>(client);
		}
		
		@Override
		public void handleMessage(Message msg) {
			ApplicationStatus status = new ApplicationStatus(msg);
			ApplicationStatusClient client = clientReference.get();
			if (client != null) {
				if (client.bound) // Just in case they have already unsubscribed
					client.listener.onChange(status);
			}
			else {
				// Can't do much here
				Log.w(getClass().getSimpleName(), "Received client message but client has been garbage collected");
			}
		}
		
	}
	
}
