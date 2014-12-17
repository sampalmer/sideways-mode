package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import android.os.Vibrator;

public class Recalibration extends State {

	private RecalibrationOverlay overlay;
	private OrientationCalculator calculator;
	private OrientationSensorHandler sensor;
	private Vibrator vibrator;
	private Orientation device;
	
	public Recalibration(RecalibrationOverlay overlay, OrientationCalculator calculator, OrientationSensorHandler sensor, Vibrator vibrator) {
		this.overlay = overlay;
		this.calculator = calculator;
		this.sensor = sensor;
		this.vibrator = vibrator;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		overlay.ensureStarted();
		
		device = Orientation.NATURAL;
		sensor.onRotationChanged().subscribe(subscriber);
		
		vibrator.vibrate(40);
	}

	@Override
	protected void stop() throws EnvironmentException {
		sensor.onRotationChanged().unsubscribe(subscriber);
		overlay.ensureStopped();
	}
	
	private final EventHandler<Orientation> subscriber = new EventHandler<Orientation>() {
		
		@Override
		public void update(Orientation newDevice) throws EnvironmentException {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0) {
				vibrator.vibrate(85);
				calculator.offsetUser(deviceChange);
			}
			
			device = newDevice;
		}
	};

}
