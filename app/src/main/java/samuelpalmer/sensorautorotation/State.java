package samuelpalmer.sensorautorotation;


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

	protected boolean started() {
		return started;
	}

	protected abstract void start();

	protected abstract void stop();
}
