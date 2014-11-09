package samuelpalmer.sensorautorotation.landscapetriggering;

public abstract class Monitor {
	
	private Listener listener;

	public Monitor(Listener listener) {
		this.listener = listener;
	}

	protected void trigger() {
		ensureRunning();
		listener.trigger();
		stop();
	}
	
	public void stop() {
		ensureRunning();
		listener = null;
	}

	private void ensureRunning() {
		if (stopped())
			throw new RuntimeException("Monitoring has stopped");
	}

	private boolean stopped() {
		return listener == null;
	}
	
}
