package samuelpalmer.sensorautorotation;

import android.util.Log;

public abstract class ExternalResource {
	private boolean stopped;
	
	public void ensureStarted() {
		if (stopped) {
			start();
			stopped = false;
		} else
			log("Already started");
	}
	
	public void ensureStopped() {
		if (!stopped) {
			stop();
			stopped = true;
		}
		else
			log("Already stopped");
	}

	
	protected abstract void start();
	protected abstract void stop();
	
	private void log(String message) {
		Log.w(getClass().getName(), new Throwable(message));
	}
}
