package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;


public class RecalibrationController extends ExternalResource {
	
	private RecalibrationOverlay overlay;
	private ProximitySensorHandler proximitySensor;
	
	public RecalibrationController(ProximitySensorHandler proximitySensor, RecalibrationOverlay overlay) {
		this.proximitySensor = proximitySensor;
		this.overlay = overlay;
	}
	
	@Override
	protected void start() {
		proximitySensor.onChange().subscribe(subscriber);
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		proximitySensor.onChange().unsubscribe(subscriber);
		
		overlay.ensureStopped();
	}
	
	private final EventHandler<ProximitySensorCoveredState> subscriber = new EventHandler<ProximitySensorCoveredState>() {
		@Override
		public void update(ProximitySensorCoveredState event) throws EnvironmentException {
			if (event.isCovered())
				overlay.ensureStarted();
			else
				overlay.ensureStopped();
		}
	};
	
}
