package samuelpalmer.sensorautorotation.screen;

import android.os.RemoteException;
import android.view.IRotationWatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ScreenRotationWatcher extends State {

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private final Object windowManagerService;
	private final Method watchRotation;
	private final Method removeRotationWatcher;

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	public ScreenRotationWatcher() {
		//noinspection TryWithIdenticalCatches
		try {
			Class<?> windowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");
			Method getWindowManagerService = windowManagerGlobal.getMethod("getWindowManagerService");
			windowManagerService = getWindowManagerService.invoke(null);
			watchRotation = windowManagerService.getClass().getMethod("watchRotation", IRotationWatcher.class);
			removeRotationWatcher = windowManagerService.getClass().getMethod("removeRotationWatcher", IRotationWatcher.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void start() {
		//noinspection TryWithIdenticalCatches
		try {
			watchRotation.invoke(windowManagerService, screenRotationChanged);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void stop() {
		//noinspection TryWithIdenticalCatches
		try {
			removeRotationWatcher.invoke(windowManagerService, screenRotationChanged);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private final IRotationWatcher.Stub screenRotationChanged = new IRotationWatcher.Stub() {
		@Override
		public void onRotationChanged(int rotation) throws RemoteException {
			onChange.report(null);
		}
	};

}
