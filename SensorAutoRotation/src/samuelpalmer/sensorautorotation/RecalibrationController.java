package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;


public class RecalibrationController extends ExternalResource {
	
	private ProximitySensorHandler proximitySensor;
	private Recalibration recalibration;
	
	public RecalibrationController(ProximitySensorHandler proximitySensor, Recalibration recalibration) {
		this.proximitySensor = proximitySensor;
		this.recalibration = recalibration;
	}
	
	@Override
	protected void start() {
		proximitySensor.onChange().subscribe(subscriber);
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		proximitySensor.onChange().unsubscribe(subscriber);
		
		recalibration.ensureStopped();
	}
	
	private final EventHandler<ProximitySensorCoveredState> subscriber = new EventHandler<ProximitySensorCoveredState>() {
		@Override
		public void update(ProximitySensorCoveredState event) throws EnvironmentException {
			if (event.isCovered())
				recalibration.ensureStarted();
			else
				recalibration.ensureStopped();
		}
	};
	
}
