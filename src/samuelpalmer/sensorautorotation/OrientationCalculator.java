package samuelpalmer.sensorautorotation;

public interface OrientationCalculator {

	public abstract void registerNewDeviceOrientation(int deviceOrientation);

	public abstract void registerNewCoveredState(boolean covered);

	public abstract int getScreenOrientation();

}