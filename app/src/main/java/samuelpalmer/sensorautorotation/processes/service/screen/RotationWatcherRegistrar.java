package samuelpalmer.sensorautorotation.processes.service.screen;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.IRotationWatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.BuildConfig;

/**
 * Single point of entry to RotationWatcher. Protects against duplicate subscriptions and handles
 * inability to unsubscribe on older versions of Android.
 */
public class RotationWatcherRegistrar extends EventRegistrarAdaptor {

	private static RotationWatcherRegistrar instance;

	public static RotationWatcherRegistrar instance() throws RotationWatcherNotSupportedException {
		if (instance == null)
			instance = new RotationWatcherRegistrar();
		return instance;
	}

	private final Handler callerHandler = new Handler(Looper.getMainLooper());
	private final Object windowManagerService;
	private final Method watchRotation;
	private final Method removeRotationWatcher;
	private boolean subscribed;

	private RotationWatcherRegistrar() throws RotationWatcherNotSupportedException {
		//noinspection TryWithIdenticalCatches
		try {
			Class<?> serviceManager = Class.forName("android.os.ServiceManager");
			IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
			Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
			windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
			watchRotation = windowManagerService.getClass().getMethod("watchRotation", IRotationWatcher.class);
			removeRotationWatcher = tryFindRemoveMethod();
		} catch (ClassNotFoundException e) {
			throw new RotationWatcherNotSupportedException(e);
		} catch (ClassCastException e) {
			throw new RotationWatcherNotSupportedException(e);
		} catch (InvocationTargetException e) {
			throw new RotationWatcherNotSupportedException(e);
		} catch (NoSuchMethodException e) {
			throw new RotationWatcherNotSupportedException(e);
		} catch (IllegalAccessException e) {
			throw new RotationWatcherNotSupportedException(e);
		}
	}

	/**
	 * Since looking through the SDK indicates the remove method was only introduced in Android 4.3.
	 */
	private Method tryFindRemoveMethod() {
		try {
			return windowManagerService.getClass().getMethod("removeRotationWatcher", IRotationWatcher.class);
		}
		catch (NoSuchMethodException ignored) {
			return null;
		}
	}

	@Override
	protected void subscribeToAdaptee() throws RotationWatcherNotSupportedException {
		if (!subscribed) { //Might already be subscribed since unsubscription is impossible on older Android versions
			Object watchResult;
			//noinspection TryWithIdenticalCatches
			try {
				watchResult = watchRotation.invoke(windowManagerService, screenRotationChanged);
			} catch (IllegalAccessException e) {
				throw new RotationWatcherNotSupportedException(e);
			} catch (InvocationTargetException e) {
				throw new RotationWatcherNotSupportedException(e);
			}

			subscribed = true;

			try {
				int currentRotationOffset;
				try {
					currentRotationOffset = (int) watchResult;
				} catch (ClassCastException e) {
					throw new RotationWatcherNotSupportedException(e);
				}

				try {
					currentRotation = wrap(currentRotationOffset);
				} catch (IllegalArgumentException e) {
					throw new RotationWatcherNotSupportedException(e);
				}
			}
			catch (Exception e) {
				try {
					unsubscribeFromAdaptee();
				} catch (Exception unsubscribeError) {
					Log.w(RotationWatcherRegistrar.class.getSimpleName(), unsubscribeError);
				}

				throw e; //Must preserve e rather than wrapping it since it might contain an exception that will be caught higher up.
			}
		}
	}

	@Override
	protected void unsubscribeFromAdaptee() throws RotationWatcherNotSupportedException {
		currentRotation = null;

		if (removeRotationWatcher != null) {
			//noinspection TryWithIdenticalCatches
			try {
				removeRotationWatcher.invoke(windowManagerService, screenRotationChanged);
			} catch (IllegalAccessException e) {
				throw new RotationWatcherNotSupportedException(e);
			} catch (InvocationTargetException e) {
				throw new RotationWatcherNotSupportedException(e);
			}

			subscribed = false;
		}
	}

	private final IRotationWatcher.Stub screenRotationChanged = new IRotationWatcher.Stub() {
		@Override
		public void onRotationChanged(final int rotation) throws RemoteException {
			if (BuildConfig.DEBUG)
				Log.d(RotationWatcherChangeDetector.class.getSimpleName(), "Got rotation change: " + rotation);

			//We seem to get here in a different thread to the one in which we subscribed.
			callerHandler.post(new Runnable() {
				@Override
				public void run() {
					//It's possible that we have tried to unsubscribe but been unable to, in which case we'll just discard any further updates we receive.
					if (subscribed) {
						try {
							currentRotation = wrap(rotation);
						} catch (IllegalArgumentException e) {
							badRotationReceivedError = e;
						}

						report();
					}
				}
			});
		}
	};

	private Orientation currentRotation;
	private Exception badRotationReceivedError;

	public Orientation lastRotationOrNull() throws RotationWatcherNotSupportedException {
		if (badRotationReceivedError != null)
			throw new RotationWatcherNotSupportedException(badRotationReceivedError);

		return currentRotation;
	}

	private static Orientation wrap(int rotation) throws IllegalArgumentException {
		return new AndroidSurfaceRotation(rotation).orientation();
	}

	public static class RotationWatcherNotSupportedException extends RuntimeException {

		public RotationWatcherNotSupportedException(Exception inner) {
			super(inner);
		}
		public RotationWatcherNotSupportedException(String message) {
			super(message);
		}

	}
}
