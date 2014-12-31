package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.media.AudioManager;

public class EarpieceStateMonitor extends StateMonitor<EarpieceSpeakerState> {

	private EarpieceSpeakerState state;

	public EarpieceStateMonitor(Context context, Listener<EarpieceSpeakerState> listener) {
		super(listener);

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		state = new EarpieceSpeakerState(audioManager);
	}

	@Override
	protected void startInternal() {
		repeater.ensureStarted();
	}

	@Override
	protected void stop() {
		repeater.ensureStopped();
	}

	private void poll() {
		reportChange(state);
	}

	private final Runnable poller = new Runnable() {

		@Override
		public void run() {
			poll();
		}
	};

	private final RecurringRunner repeater = new RecurringRunner(poller, 500);

	@Override
	protected EarpieceSpeakerState get() {
		return state;
	}

}
