package samuelpalmer.sensorautorotation;

import android.os.Handler;

public class RecurringRunner {
	
	private Handler handler;
	private Runnable runnable;
	private int periodInMilliseconds;

	public RecurringRunner(Runnable runnable, int periodInMilliseconds) {
		this.handler = new Handler();
		this.runnable = runnable;
		this.periodInMilliseconds = periodInMilliseconds;
	}
	
	private Runnable runAndEnqueue = new Runnable() {
		@Override
		public void run() {
			runnable.run();
			handler.postDelayed(runAndEnqueue, periodInMilliseconds);
		}
	};
	
	public void start() {
		runAndEnqueue.run();
	}
	
	public void stop() {
		handler.removeCallbacks(runAndEnqueue);
	}
}
