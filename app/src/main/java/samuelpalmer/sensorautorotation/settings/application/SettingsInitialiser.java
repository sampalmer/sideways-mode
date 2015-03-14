package samuelpalmer.sensorautorotation.settings.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Setting;

public class SettingsInitialiser {

	private final ApplicationSettings source;
	private final ArrayList<Setting<?>> settings = new ArrayList<>();

	public SettingsInitialiser(ApplicationSettings source) {
		this.source = source;

		for (Method publicMethod : publicMethods())
			new CandidateMethod(publicMethod).consider();

		if (settings.isEmpty())
			throw new RuntimeException("Couldn't identify application settings.");
	}

	private Method[] publicMethods() {
		return source.getClass().getMethods();
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
			try {
				return (Setting<?>)method.invoke(source);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
