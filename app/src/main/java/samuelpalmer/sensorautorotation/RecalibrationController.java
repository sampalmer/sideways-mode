package samuelpalmer.sensorautorotation;

import android.os.Handler;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;


public class RecalibrationController extends State {

	private ProximitySensorHandler proximitySensor;
	private final Handler handler;

	public RecalibrationController(ProximitySensorHandler proximitySensor) {
		this.proximitySensor = proximitySensor;
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

		dequeueUpdates();
	}

	private final EventHandler<ProximitySensorCoveredState> subscriber = new EventHandler<ProximitySensorCoveredState>() {
		@Override
		public void update(ProximitySensorCoveredState event) {
			if (event.isCovered() != covered) {
				covered = event.isCovered();
				if (covered)
					handler.postDelayed(startRecalibration, 400);
				else
					dequeueUpdates();
			}
		}
	};

	private final Runnable startRecalibration = new Runnable() {
		@Override
		public void run() {
			try {
				RotationService.overlay.ensureStarted();
			} catch (EnvironmentException e) {
				RotationService.errorHandler.handle(e);
			}
		}
	};

	private void dequeueUpdates() {
		handler.removeCallbacks(startRecalibration);
	}

	private boolean covered;
}
