package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import samuelpalmer.sensorautorotation.components.RecalibrationActivity;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;


public class RecalibrationController extends State {

	private Context context;
	private ProximitySensorHandler proximitySensor;
	private final Handler handler;

	public RecalibrationController(Context context, ProximitySensorHandler proximitySensor) {
		this.context = context;
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
			Intent intent = new Intent(context, RecalibrationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	};

	private void dequeueUpdates() {
		handler.removeCallbacks(startRecalibration);
	}

	private boolean covered;
}
