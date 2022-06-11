package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides access to remotely monitor the state of the service.
 * Not using a content provider for this since they have various issues (randomly returning null, provider can't even be found in rare situations, poor error handling, blocking I/O)
 */
public class ApplicationStatusService extends Service {
    
    public static final int MSG_SUBSCRIBE = 1;
    public static final int MSG_UNSUBSCRIBE = 2;
    
    private final Set<Messenger> clients = new HashSet<>();
    private ApplicationStatusMonitor applicationStatusMonitor;
    private boolean isCreated;
    
    private static class ServerHandler extends Handler {
        
        // Using a WeakReference because Android Studio said to do so
        private final WeakReference<ApplicationStatusService> serviceReference;
        
        private ServerHandler(WeakReference<ApplicationStatusService> serviceReference) {
            super(Looper.getMainLooper());
            this.serviceReference = serviceReference;
        }
        
        @Override
        public void handleMessage(Message msg) {
            ApplicationStatusService service = serviceReference.get();
            if (service == null) {
                Log.w(ApplicationStatusService.class.getSimpleName(), "Received message after service was garbage collected. Ignoring.");
                return;
            }
            
            if (!service.isCreated) {
                Log.w(ApplicationStatusService.class.getSimpleName(), "Received message after service onDestroy(). Ignoring.");
                return;
            }
            
            switch (msg.what) {
                case MSG_SUBSCRIBE: {
                    final Messenger newClient = msg.replyTo;
                    boolean isNewSubscription = service.clients.add(newClient);
                    
                    if (isNewSubscription) {
                        ApplicationStatus status = service.applicationStatusMonitor.getStatus();
                        
                        if (status != null) {
                            Message message = status.asMessage();
                            
                            try {
                                newClient.send(message);
                            } catch (RemoteException e) {
                                // The client is dead. Remove it from the list.
                                service.clients.remove(newClient);
                            }
                        }
                    }
                    else
                        Log.w(ApplicationStatusService.class.getSimpleName(), "Received duplicate subscription");
                    
                    break;
                }
                case MSG_UNSUBSCRIBE: {
                    boolean wasSubscribed = service.clients.remove(msg.replyTo);
                    if (!wasSubscribed)
                        Log.w(ApplicationStatusService.class.getSimpleName(), "Received unsubscription for non-subscriber");
                    
                    break;
                }
                default:
                    throw new RuntimeException("Received unknown message: " + msg.what);
            }
        }
        
    }
    
    private final Messenger messenger = new Messenger(new ServerHandler(new WeakReference<>(this)));
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        isCreated = true;
        
        applicationStatusMonitor = new ApplicationStatusMonitor(this);
        applicationStatusMonitor.subscribe(statusChanged);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        isCreated = false;
        
        applicationStatusMonitor.unsubscribe(statusChanged);
        applicationStatusMonitor = null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        throw new RuntimeException("This service cannot be started; it is only for binding.");
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }
    
    private final EventHandler statusChanged = new EventHandler() {
        @Override
        public void update() {
            ApplicationStatus status = applicationStatusMonitor.getStatus();
            if (status == null)
                return;
            
            Message message = status.asMessage();
            
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
    };
    
}
