package samuelpalmer.common.contentprovidersharedpreferences;

import android.os.IBinder;

class ClientListenerRecord {
	
	public final ClientListener listener;
	public final IBinder serverBinder;
	public final IBinder.DeathRecipient deathRecipient;
	
	public ClientListenerRecord(ClientListener listener, IBinder serverBinder, IBinder.DeathRecipient deathRecipient) {
		
		this.listener = listener;
		this.serverBinder = serverBinder;
		this.deathRecipient = deathRecipient;
	}
	
}
