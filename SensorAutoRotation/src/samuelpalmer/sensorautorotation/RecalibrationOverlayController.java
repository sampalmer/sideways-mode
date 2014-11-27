package samuelpalmer.sensorautorotation;


public class RecalibrationOverlayController
	extends ExternalResource
	implements ProximitySensorHandler.Listener {
	
	private RecalibrationOverlay overlay;
	
	public RecalibrationOverlayController(RecalibrationOverlay overlay) {
		this.overlay = overlay;
	}
	
	@Override
	protected void start() {
		
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		overlay.ensureStopped();
	}
	
	@Override
	public void update(boolean covered) throws EnvironmentException {
		if (covered)
			overlay.ensureStarted();
		else
			overlay.ensureStopped();
	}
	
}
