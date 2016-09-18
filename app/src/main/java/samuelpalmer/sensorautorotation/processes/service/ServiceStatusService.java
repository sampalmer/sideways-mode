package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class ServiceStatusService extends Service {

	public static final int MSG_SUBSCRIBE = 1;

	private final Messenger messenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SUBSCRIBE:
					if (msg.replyTo == null)
						throw new RuntimeException("No reply-to messenger provided");



					break;
				default:
					throw new RuntimeException("Unrecognised message type: " + msg.what);
			}
		}
	});

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
