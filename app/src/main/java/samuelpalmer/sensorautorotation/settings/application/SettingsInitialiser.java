package samuelpalmer.sensorautorotation.settings.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;

/**
 * We're using this instead of {@link android.preference.PreferenceManager#setDefaultValues} since
 * that doesn't actually set the default values when they're "false" on Android 4.0.4.
 */
public class SettingsInitialiser {

	private final ArrayList<Setting<?>> settings = new ArrayList<>();
	private final ApplicationSettings applicationSettings;

	public SettingsInitialiser(ApplicationSettings applicationSettings) {
		this.applicationSettings = applicationSettings;

		for (Method publicMethod : publicMethods())
			new CandidateMethod(publicMethod).consider();

		if (settings.isEmpty())
			throw new RuntimeException("Couldn't identify application settings.");
	}

	private Method[] publicMethods() {
		return ApplicationSettings.class.getMethods();
	}

	public void ensureInitialised() {
		for (Setting<?> setting : settings)
			setting.ensureInitialised();
	}

	public class CandidateMethod {
		private final Method method;

		public CandidateMethod(Method method) {
			this.method = method;
		}

		public void consider() {
			if (isSetting())
				settings.add(getSetting());
		}

		private boolean isSetting() {
			return method.getReturnType() == Setting.class;
		}

		private Setting<?> getSetting() {
			//Suppressing this warning because Android Studio says we need to do it this way
			// for compatibility with API 19-.
			//noinspection TryWithIdenticalCatches
			try {
				return (Setting<?>)method.invoke(applicationSettings);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
