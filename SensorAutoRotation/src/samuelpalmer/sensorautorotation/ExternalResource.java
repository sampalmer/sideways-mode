package samuelpalmer.sensorautorotation;


public abstract class ExternalResource {
	private boolean started;
	
	public void ensureStarted() throws EnvironmentException {
		if (!started) {
			start();
			started = true;
		}
	}
	
	public void ensureStopped() {
		if (started) {
			stop();
			started = false;
		}
	}
	
	protected abstract void start() throws EnvironmentException;
	protected abstract void stop();
}
