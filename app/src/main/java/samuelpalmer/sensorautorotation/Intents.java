package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Intents {

	private final Context context;
	private final Class<?> clazz;

	public Intents(Context context, Class<?> clazz) {
		this.context = context;
		this.clazz = clazz;
	}

	public PendingIntent makePending(Intent intent) {
		int requestCode = 0;
		int flags = PendingIntent.FLAG_CANCEL_CURRENT;

		Class<?> ancestorClass = clazz;

		while (ancestorClass != null) {
			ancestorClass = ancestorClass.getSuperclass();
			if (ancestorClass == Activity.class)
				return PendingIntent.getActivity(context, requestCode, intent, flags);
			else if (ancestorClass == BroadcastReceiver.class)
				return PendingIntent.getBroadcast(context, requestCode, intent, flags);
			else if (ancestorClass == Service.class)
				return PendingIntent.getService(context, requestCode, intent, flags);
		}

		throw new RuntimeException(clazz + " doesn't appear to be a component");
	}

	public PendingIntent makePending() {
		return makePending(makeIntent());
	}

	public Intent makeIntent() {
		return new Intent(context, clazz);
	}

}
