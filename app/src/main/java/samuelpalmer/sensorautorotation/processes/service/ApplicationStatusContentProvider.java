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
import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;

import samuelpalmer.common.ParcelableBinder;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplay;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IApplicationStatusListener;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ApplicationStatusContentProvider extends ContentProvider {
	
	private static final ConcurrentHashMap<IBinder, ServerListener> listeners = new ConcurrentHashMap<>();
	private static final String METHOD_SUBSCRIBE = "subscribe";
	private static final String METHOD_UNSUBSCRIBE = "unsubscribe";
	private static final String KEY_SUCCESS = "success";
	
	@Override
	public boolean onCreate() { return true; }

	@SuppressWarnings("unchecked")
	@Override
	public Bundle call(@NonNull String method, String unused, Bundle extras) {
		switch (method)
		{
			case METHOD_SUBSCRIBE: {
				final IBinder binder = getBinder(extras);
				final ServerListener listener = subscribe(binder);

				if (listener != null) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (ApplicationInstanceManager.isApplicationRunning()) {
								ApplicationInstance application = ApplicationInstanceManager.applicationInstance();
								
								//TODO: Replace all this with an EventRegistrarAdaptor and ApplicationStatusDisplays.add()
								ApplicationStatusDisplayUpdater manager = application.applicationStatusDisplayUpdater;
								ApplicationStatus lastStatus = manager.getLastStatus();
								if (lastStatus != null)
									listener.reportUpdate(lastStatus);
							}
						}
					});
					
					/*
					 * If the service has crashed or simply hasn't yet started (due to a phone restart, for example),
					 * we want to make sure it's running. Otherwise the client will have to wait
					 * a long time (or forever) to get the first service status update.
					 */
					new ServiceSynchroniser(ApplicationSettings.instance()).updateFromSettings();
				}

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
		
		Bundle result = new Bundle();
		result.putBoolean(KEY_SUCCESS, true); //Confirms to the caller that the method didn't crash
		return result;
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
		IApplicationStatusListener listener = IApplicationStatusListener.Stub.asInterface(binder);

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

		if (removed != null)
			binder.unlinkToDeath(removed.deathRecipient, 0);
	}

	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		throw notSupported();
	}

	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		throw notSupported();
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	private RuntimeException notSupported() {
		return new RuntimeException("Not supported");
	}

	private static class ServerListener
	{
		private final IApplicationStatusListener listener;
		private final IBinder.DeathRecipient deathRecipient;

		public ServerListener(IApplicationStatusListener listener, IBinder.DeathRecipient deathRecipient) {
			this.listener = listener;
			this.deathRecipient = deathRecipient;
		}

		public void reportUpdate(ApplicationStatus status) {
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

		public void subscribe(IApplicationStatusListener listener) {
			call(METHOD_SUBSCRIBE, listener);
		}

		public void unsubscribe(IApplicationStatusListener listener) {
			call(METHOD_UNSUBSCRIBE, listener);
		}

		private void call(String method, IApplicationStatusListener listener) {
			Bundle args = new Bundle();
			args.putParcelable(null, new ParcelableBinder(listener.asBinder()));

			Bundle result = contentResolver.call(
				new Uri.Builder()
					.scheme("content")
					.authority(StringResource.get(R.string.status_provider_authority).toString())
					.build(),
					method, null, args
			);
			
			// If the content provider crashes, it can return null or an empty bundle.
			if (result == null || !result.getBoolean(KEY_SUCCESS, false))
				throw new RuntimeException("Didn't get a result. The content provider probably crashed.");
		}

	}

	public static class RemoteStatusDisplay implements ApplicationStatusDisplay {

		@Override
		public void onApplicationStopped() {}

		@Override
		public void update(ApplicationStatus applicationStatus) {
			for (ServerListener listener : listeners.values())
				listener.reportUpdate(applicationStatus);
		}

	}

}
