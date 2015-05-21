package samuelpalmer.sensorautorotation.screen;

import android.os.IBinder;
import android.os.RemoteException;
import android.view.IRotationWatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Supports detection of all four screen rotations using a hidden API
 */
class RotationWatcherChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final Object windowManagerService;
	private final Method watchRotation;
	private final Method removeRotationWatcher;
	private final ScreenRotationMonitor.Callback callback;

	public RotationWatcherChangeDetector(ScreenRotationMonitor.Callback callback) throws NotSupportedException {
		this.callback = callback;

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
			watchRotation.invoke(windowManagerService, screenRotationChanged);
		} catch (IllegalAccessException e) {
			throw new NotSupportedException(e);
		} catch (InvocationTargetException e) {
			throw new NotSupportedException(e);
		}
	}

	@Override
	public void stop() throws NotSupportedException {
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

	public class NotSupportedException extends RuntimeException {
		NotSupportedException(Exception inner) {
			super(inner);
		}
	}

	private final IRotationWatcher.Stub screenRotationChanged = new IRotationWatcher.Stub() {
		@Override
		public void onRotationChanged(int rotation) throws RemoteException {
			callback.changed();
		}
	};

}
