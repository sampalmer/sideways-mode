package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;

import samuelpalmer.common.ParcelableBinder;

class RegisterOnSharedPreferenceChangeListenerContract extends OnSharedPreferenceChangeListenerContract<IBinder> {
	
	private static final String SERVER_BINDER = "serverbinder";
	
	@Override
	public String methodName() {
		return "registerOnSharedPreferenceChangeListener";
	}

	@Override
	public IBinder process(final SharedPreferences sharedPreferences, final IContentProviderSharedPreferenceChangeListener listener) {

		final IBinder binder = listener.asBinder();

		DeathRecipient dr = new DeathRecipient() {
			@Override
			public void binderDied() {
				unsubscribe(sharedPreferences, binder);
			}
		};

		ServerListener serverListener = new ServerListener(listener, dr);
		sharedPreferences.registerOnSharedPreferenceChangeListener(serverListener.nativeListener);
		
		// Sending back a binder so the client can determine when the server process dies.
		IBinder result = new Binder();
		
		try {
			binder.linkToDeath(dr, 0);
		} catch (RemoteException e) {
			// The client process has died
			return result;
		}

		synchronized (listeners) {
			listeners.put(binder, serverListener);
		}

		return result;
	}
	
	@Override
	public Bundle serialiseResult(IBinder value) {
		Bundle result = new Bundle(1);
		SingleValueSerialiser.serialise(result, SERVER_BINDER, value);
		return result;
	}
	
	@Override
	public IBinder deserialiseResult(Bundle serialised) {
		serialised.setClassLoader(ParcelableBinder.class.getClassLoader());
		return (IBinder) SingleValueSerialiser.deserialise(serialised.get(SERVER_BINDER));
	}
	
}
