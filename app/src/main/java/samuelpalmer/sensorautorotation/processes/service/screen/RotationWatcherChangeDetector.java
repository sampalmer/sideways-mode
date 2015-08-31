package samuelpalmer.sensorautorotation.processes.service.screen;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.IRotationWatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor.Callback;

/**
 * Supports detection of all four screen rotations using a hidden API
 */
public class RotationWatcherChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final Object windowManagerService;
	private final Method watchRotation;
	private final Method removeRotationWatcher;
	private final ScreenRotationMonitor.Callback callback;
	private final IRotationWatcher.Stub screenRotationChanged;
	private final Handler callerHandler;
	private final Screen screen;

	public RotationWatcherChangeDetector(Callback callback, Screen screen) throws NotSupportedException {
		callerHandler = new Handler();
		this.callback = callback;
		this.screen = screen;

		screenRotationChanged = new Watcher();

		//noinspection TryWithIdenticalCatches
		try {
			Class<?> serviceManager = Class.forName("android.os.ServiceManager");
			IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
			Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
			windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
			watchRotation = windowManagerService.getClass().getMethod("watchRotation", IRotationWatcher.class);
			removeRotationWatcher = tryFindRemoveMethod();
		} catch (ClassNotFoundException e) {
			throw new NotSupportedException(e);
		} catch (ClassCastException e) {
			throw new NotSupportedException(e);
		} catch (InvocationTargetException e) {
			throw new NotSupportedException(e);
		} catch (NoSuchMethodException e) {
			throw new NotSupportedException(e);
		} catch (IllegalAccessException e) {
			throw new NotSupportedException(e);
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
	public void start() throws NotSupportedException {
		//noinspection TryWithIdenticalCatches
		try {
			currentRotation = wrap((int)watchRotation.invoke(windowManagerService, screenRotationChanged));
		} catch (IllegalAccessException e) {
			throw new NotSupportedException(e);
		} catch (InvocationTargetException e) {
			throw new NotSupportedException(e);
		}
	}

	@Override
	public void stop() throws NotSupportedException {
		currentRotation = null;

		if (removeRotationWatcher != null) {
			//noinspection TryWithIdenticalCatches
			try {
				removeRotationWatcher.invoke(windowManagerService, screenRotationChanged);
			} catch (IllegalAccessException e) {
				throw new NotSupportedException(e);
			} catch (InvocationTargetException e) {
				throw new NotSupportedException(e);
			}
		}
	}

	public static class NotSupportedException extends RuntimeException {

		public NotSupportedException(Exception inner) {
			super(inner);
		}
		public NotSupportedException(String message) {
			super(message);
		}

	}

	private Orientation currentRotation;

	@Override
	public Orientation currentRotation() {
		if (currentRotation != null)
			return currentRotation;
		else
			return screen.realOrientation();
	}

	private class Watcher extends IRotationWatcher.Stub {
		@Override
		public void onRotationChanged(final int rotation) throws RemoteException {
			//We seem to get here in a different thread to the one in which we subscribed.
			callerHandler.post(new Runnable() {
				@Override
				public void run() {
					currentRotation = wrap(rotation);
					callback.changed();
				}
			});
		}
	}

	private static Orientation wrap(int rotation) {
		return new AndroidSurfaceRotation(rotation).orientation();
	}

}
