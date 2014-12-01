package samuelpalmer.sensorautorotation;

public class Recalibration extends ExternalResource {

	private RecalibrationOverlay overlay;

	public Recalibration(RecalibrationOverlay overlay) {
		this.overlay = overlay;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		overlay.ensureStarted();
	}

	@Override
	protected void stop() throws EnvironmentException {
		overlay.ensureStopped();
	}

}
