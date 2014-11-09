package samuelpalmer.sensorautorotation.landscapetriggering;

import samuelpalmer.sensorautorotation.EnvironmentException;
import android.os.Handler;

public abstract class Monitor {
	
	private Listener listener;

	public Monitor(final Listener listener, int timeoutMilliseconds) {
		this.listener = listener;
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (!stopped())
					stop();
			}
			
		},
		timeoutMilliseconds);
	}

	protected void trigger() throws EnvironmentException {
		ensureRunning();
		listener.trigger();
		stop();
	}
	
	public void stop() {
		ensureRunning();
		listener.stop();
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
