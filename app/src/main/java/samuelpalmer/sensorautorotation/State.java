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

	/**
	 * Throws an exception if {@link #started()} is {@code false}
	 */
	protected void verifyStarted() {
		if (!started())
			throw new RuntimeException("Not started");
	}

	protected abstract void start();

	protected abstract void stop();
}
