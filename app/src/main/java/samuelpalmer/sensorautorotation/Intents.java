package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Intents {

	public static final int FLAGS = PendingIntent.FLAG_CANCEL_CURRENT;
	public static final int REQUEST_CODE = 0;

	private final Context context;
	private final Class<?> clazz;

	public Intents(Context context, Class<?> clazz) {
		this.context = context;
		this.clazz = clazz;
	}

	public PendingIntent makePending(Intent intent) {
		Class<?> ancestorClass = clazz;

		while (ancestorClass != null) {
			ancestorClass = ancestorClass.getSuperclass();
			if (ancestorClass == Activity.class)
				return PendingIntent.getActivity(context, REQUEST_CODE, intent, FLAGS);
			else if (ancestorClass == BroadcastReceiver.class)
				return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, FLAGS);
			else if (ancestorClass == Service.class)
				return PendingIntent.getService(context, REQUEST_CODE, intent, FLAGS);
		}

		throw new RuntimeException(clazz + " doesn't appear to be a component");
	}

	public Intent makeIntent() {
		return new Intent(context, clazz);
	}

}
