package samuelpalmer.sensorautorotation.processes.service;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.concurrent.ConcurrentHashMap;

import samuelpalmer.common.ParcelableBinder;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplay;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;

public class ServiceStatusContentProvider extends ContentProvider {

	private static final ConcurrentHashMap<IBinder, ServerListener> listeners = new ConcurrentHashMap<>();
	private static final String METHOD_SUBSCRIBE = "subscribe";
	private static final String METHOD_UNSUBSCRIBE = "unsubscribe";

	@Override
	public boolean onCreate() { return true; }

	@SuppressWarnings("unchecked")
	@Override
	public Bundle call(String method, String unused, Bundle extras) {
		switch (method)
		{
			case METHOD_SUBSCRIBE: {
				final IBinder binder = getBinder(extras);
				final ServerListener listener = subscribe(binder);

				if (listener != null)
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (ServiceHolder.isRunning()) {
								RotationService service = ServiceHolder.instance();

								//TODO: Replace all this with an EventRegistrarAdaptor and ServiceStatusDisplays.add()
								ServiceStatusDisplayUpdater manager = service.serviceStatusDisplayUpdater;
								ServiceStatus lastStatus = manager.getLastStatus();
								if (lastStatus != null)
									listener.reportUpdate(lastStatus);
							}
						}
					});

				break;
			}
			case METHOD_UNSUBSCRIBE: {
				final IBinder binder = getBinder(extras);
				unsubscribe(binder);
				break;
			}
			default:
				throw notSupported();
		}

		return new Bundle(); //Confirms to the caller that the method succeeded
	}

	private IBinder getBinder(Bundle extras) {
		extras.setClassLoader(ParcelableBinder.class.getClassLoader());
		ParcelableBinder wrapper = extras.getParcelable(null);
		assert wrapper != null;
		return wrapper.binder;
	}

	/**
	 * @return {@code null} if {@code binder} dies
	 */
	private ServerListener subscribe(final IBinder binder) {
		IServiceStatusListener listener = IServiceStatusListener.Stub.asInterface(binder);

		IBinder.DeathRecipient dr = new IBinder.DeathRecipient() {
			@Override
			public void binderDied() {
				unsubscribe(binder);
			}
		};

		ServerListener serverListener = new ServerListener(listener, dr);

		try {
			binder.linkToDeath(dr, 0);
		} catch (RemoteException e) {
			return null; //Binder process has already died
		}

		listeners.put(binder, serverListener);

		return serverListener;
	}

	private void unsubscribe(IBinder binder) {
		ServerListener removed = listeners.remove(binder);

		if (binder != null)
			binder.unlinkToDeath(removed.deathRecipient, 0);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		throw notSupported();
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw notSupported();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	private RuntimeException notSupported() {
		return new RuntimeException("Not supported");
	}

	private static class ServerListener
	{
		private final IServiceStatusListener listener;
		private final IBinder.DeathRecipient deathRecipient;

		public ServerListener(IServiceStatusListener listener, IBinder.DeathRecipient deathRecipient) {
			this.listener = listener;
			this.deathRecipient = deathRecipient;
		}

		public void reportUpdate(ServiceStatus status) {
			try {
				listener.onChange(status);
			} catch (RemoteException ignored) {}
		}
	}

	public static class Client {

		private final ContentResolver contentResolver;

		public Client(Context context) {
			contentResolver = context.getContentResolver();
		}

		public void subscribe(IServiceStatusListener listener) {
			call(METHOD_SUBSCRIBE, listener);
		}

		public void unsubscribe(IServiceStatusListener listener) {
			call(METHOD_UNSUBSCRIBE, listener);
		}

		private void call(String method, IServiceStatusListener listener) {
			Bundle args = new Bundle();
			args.putParcelable(null, new ParcelableBinder(listener.asBinder()));

			Bundle result = contentResolver.call(
				new Uri.Builder()
					.scheme("content")
					.authority(StringResource.get(R.string.status_provider_authority).toString())
					.build(),
					method, null, args
			);

			if (result == null)
				throw new RuntimeException("Got back null. The content provider probably crashed.");
		}

	}

	public static class RemoteStatusDisplay implements ServiceStatusDisplay {

		@Override
		public void onServiceStopped() {}

		@Override
		public void update(ServiceStatus serviceStatus) {
			for (ServerListener listener : listeners.values())
				listener.reportUpdate(serviceStatus);
		}

	}

}
