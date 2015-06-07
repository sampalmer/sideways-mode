package samuelpalmer.sensorautorotation;

public abstract class StringResource {
	public static CharSequence format(int formatStringResource, Object... formatArguments) {
		return Process.context().getString(formatStringResource, lookupTextIfNecessary(formatArguments));
	}

	public static CharSequence get(int id) {
		return Process.context().getText(id);
	}

	private static Object[] lookupTextIfNecessary(Object[] formatArguments) {
		Object[] lookedUpFormatArgumentResources = new Object[formatArguments.length];
		for (int i = 0; i < formatArguments.length; ++i)
			lookedUpFormatArgumentResources[i] = lookUpTextIfNecessary(formatArguments[i]);

		return lookedUpFormatArgumentResources;
	}

	private static Object lookUpTextIfNecessary(Object currentArgument) {
		if (looksLikeResourceId(currentArgument))
			return get((int) currentArgument);
		else
			return currentArgument;
	}

	private static boolean looksLikeResourceId(Object currentArgument) {
		return currentArgument instanceof Integer;
	}
}
