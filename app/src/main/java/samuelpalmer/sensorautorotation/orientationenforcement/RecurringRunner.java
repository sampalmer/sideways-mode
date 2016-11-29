package samuelpalmer.sensorautorotation.orientationenforcement;

import android.os.Handler;

import samuelpalmer.sensorautorotation.State;

public class RecurringRunner extends State {

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

	@Override
	protected void start() {
		runAndEnqueue.run();
	}

	@Override
	protected void stop() {
		handler.removeCallbacks(runAndEnqueue);
	}
}
