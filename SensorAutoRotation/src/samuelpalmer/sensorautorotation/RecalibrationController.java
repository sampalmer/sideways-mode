package samuelpalmer.sensorautorotation;

import android.os.Handler;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;


public class RecalibrationController extends State {
	
	private ProximitySensorHandler proximitySensor;
	private Recalibration recalibration;
	
	private final Handler handler;
	private UserErrorHandler errorHandler;
	
	public RecalibrationController(ProximitySensorHandler proximitySensor, Recalibration recalibration, UserErrorHandler errorHandler) {
		this.proximitySensor = proximitySensor;
		this.recalibration = recalibration;
		this.errorHandler = errorHandler;
		handler = new Handler();
	}
	
	@Override
	protected void start() {
		covered = false;
		proximitySensor.onChange().subscribe(subscriber);
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		proximitySensor.onChange().unsubscribe(subscriber);
		
		recalibration.ensureStopped();
		dequeueUpdates();
	}
	
	private final EventHandler<ProximitySensorCoveredState> subscriber = new EventHandler<ProximitySensorCoveredState>() {
		@Override
		public void update(ProximitySensorCoveredState event) {
			if (event.isCovered() != covered) {
				covered = event.isCovered();
				if (covered)
					handler.postDelayed(r, 400);
				else {
					dequeueUpdates();
					r.run();
				}
			}
		}
	};
	
	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			try {
				if (covered)
					recalibration.ensureStarted();
				else
					recalibration.ensureStopped();
			} catch (EnvironmentException e) {
				errorHandler.handle(e);
			}
		}
	};
	
	private void dequeueUpdates() {
		handler.removeCallbacks(r);
	}

	private boolean covered;
}
