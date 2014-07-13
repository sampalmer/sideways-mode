package samuelpalmer.sensorautorotation;

public interface OrientationChanger {

	public abstract void set(Orientation value);
	public abstract Orientation getOriginal();
	public abstract void close();

}