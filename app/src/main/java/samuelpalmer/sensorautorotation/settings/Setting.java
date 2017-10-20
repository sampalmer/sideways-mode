package samuelpalmer.sensorautorotation.settings;

public interface Setting<T> {

	T get();
	void set(T value);

}
