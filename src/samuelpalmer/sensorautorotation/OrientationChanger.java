package samuelpalmer.sensorautorotation;

public interface OrientationChanger {

	public abstract void set(Orientation value);
	public abstract int get();
	public abstract void close();

}