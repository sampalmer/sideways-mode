package samuelpalmer.sensorautorotation.utilities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ViewUtilities {
	
	/**
	 * Only changes the visibility if it's different to the current value.
	 * This is to minimise lag.
	 */
	public static void ensureVisibility(View control, int newVisibility) {
		int currentVisibility = control.getVisibility();
		if (currentVisibility != newVisibility)
			control.setVisibility(newVisibility);
	}

	/**
	 * @return The root views for the app, or {@code null} on failure.
	 */
	@SuppressLint("PrivateApi")
	public static List<View> getRootViews() {
		Class<?> windowManagerClass;
		Object windowManager;

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				windowManagerClass = Class.forName("android.view.WindowManagerGlobal");
				windowManager = windowManagerClass.getMethod("getInstance").invoke(null);
			} else {
				windowManagerClass = Class.forName("android.view.WindowManagerImpl");
				windowManager = windowManagerClass.getMethod("getDefault").invoke(null);
			}

			Field viewsField = windowManagerClass.getDeclaredField("mViews");
			viewsField.setAccessible(true);
			Object views = viewsField.get(windowManager);

			if (views == null)
				// The field probably hasn't been initialised because no root views have been added to it yet.
				return Collections.emptyList();
			else if (views instanceof Iterable) {
				ArrayList<View> result = new ArrayList<>();

				for (Object view : (Iterable) views)
					if (view instanceof View)
						result.add((View) view);
					else {
						Log.w(ViewUtilities.class.getSimpleName(), "Field " + viewsField + " contains instance of " + view.getClass().getSimpleName() + " but expected " + View.class.getSimpleName());
						return null;
					}

				return result;
			} else if (views instanceof View[])
				return Arrays.asList((View[])views);
			else {
				Log.w(ViewUtilities.class.getSimpleName(), "Field " + viewsField + " has value of unexpected type: " + views.getClass().getSimpleName());
				return null;
			}
		}
		catch (ExceptionInInitializerError | ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
			Log.w(ViewUtilities.class.getSimpleName(), "Couldn't access root views", ex);
			return null;
		}
	}
}
