package samuelpalmer.sensorautorotation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Intents {

	private Context context;

	public Intents(Context context) {
		this.context = context;
	}

	public PendingIntent makePending(Intent intent, ComponentType type) {

		int flags = PendingIntent.FLAG_CANCEL_CURRENT;
		int requestCode = 0;

		switch (type) {
			case ACTIVITY:
				return PendingIntent.getActivity(context, requestCode, intent, flags);
			case BROADCAST_RECEIVER:
				return PendingIntent.getBroadcast(context, requestCode, intent, flags);
			default:
				throw new RuntimeException("Unknown type: " + type);
		}

	}

	public Intent makeIntent(Class<?> clazz) {
		return new Intent(context, clazz);
	}

	public static enum ComponentType {
		ACTIVITY,
		BROADCAST_RECEIVER
	}

}
