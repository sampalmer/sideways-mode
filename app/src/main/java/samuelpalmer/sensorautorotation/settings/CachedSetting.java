package samuelpalmer.sensorautorotation.settings;

import samuelpalmer.common.settings.Setting;

public class CachedSetting<T> implements Setting<T> {

	private final Setting<T> source;
	private boolean hasCachedValue;
	private T cachedValue;

	public CachedSetting(Setting<T> source) {
		this.source = source;
	}

	@Override
	public T get() {
		if (!hasCachedValue)
			updateCache(source.get());

		return cachedValue;
	}

	@Override
	public void set(T value) {
		source.set(value);
		updateCache(value);
	}

	public void updateCache(T value) {
		cachedValue = value;
		hasCachedValue = true;
	}

}
