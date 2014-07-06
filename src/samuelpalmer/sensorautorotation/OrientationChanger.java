package samuelpalmer.sensorautorotation;

public interface OrientationChanger {

	public abstract void set(int orientation);
	public abstract int get();
	public abstract void close();

}