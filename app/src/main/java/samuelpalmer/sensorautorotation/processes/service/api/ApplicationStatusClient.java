package samuelpalmer.sensorautorotation.processes.service.api;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;

public class ApplicationStatusClient {
	
	private final Messenger serviceMessenger;
	private final Listener listener;
	private final Messenger clientMessenger;
	
	public ApplicationStatusClient(IBinder service, Listener listener) {
		this.serviceMessenger = new Messenger(service);
		this.listener = listener;
		this.clientMessenger = new Messenger(new ClientHandler(this));
	}
	
	public void subscribe() throws RemoteException {
		Message msg = Message.obtain(null, ApplicationStatusService.MSG_SUBSCRIBE);
		msg.replyTo = clientMessenger;
		serviceMessenger.send(msg);
	}
	
	public void unsubscribe() throws RemoteException {
		Message msg = Message.obtain(null, ApplicationStatusService.MSG_UNSUBSCRIBE);
		msg.replyTo = clientMessenger;
		serviceMessenger.send(msg);
	}
	
	public interface Listener {
		void onChange(ApplicationStatus status);
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
			if (client != null)
				client.listener.onChange(status);
			else {
				// Can't do much here
				Log.w(getClass().getSimpleName(), "Received client message but client has been garbage collected");
			}
		}
		
	}
	
}
