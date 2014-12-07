package samuelpalmer.sensorautorotation.facedetection;

import android.util.Log;

public class TimingLogger {
	
	private Long lastMessage;
	
	public void showMessage(String message) {
		long now = System.nanoTime();

		if (lastMessage != null) {
			double durationMs = (now - lastMessage) / 1000000.0;
			message += " (" + Math.round(durationMs) + "ms)";
		}

		lastMessage = now;

		Log.i("Camera", message);
	}

}
