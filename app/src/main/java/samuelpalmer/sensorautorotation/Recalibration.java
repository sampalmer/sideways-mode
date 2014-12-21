package samuelpalmer.sensorautorotation;

public class Recalibration extends State {

	private RecalibrationOverlay overlay;
	private OrientationCalculator calculator;

	public Recalibration(RecalibrationOverlay overlay, OrientationCalculator calculator) {
		this.overlay = overlay;
		this.calculator = calculator;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		overlay.ensureStarted();
		calculator.setRecalibrating(true);
	}

	@Override
	protected void stop() throws EnvironmentException {
		calculator.setRecalibrating(false);
		overlay.ensureStopped();
	}

}
