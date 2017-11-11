package samuelpalmer.sensorautorotation.utilities;


public abstract class State {
	private boolean started;

	public void ensureStarted() {
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

	public boolean isStarted() {
		return started;
	}
	
	protected abstract void start();

	protected abstract void stop();
}
