package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Build;

public class Intents {

	private final Class<?> clazz;

	public Intents(Class<?> clazz) {
		this.clazz = clazz;
	}

	public PendingIntent makePending(Intent intent) {
		int requestCode = 0;
		int flags = PendingIntent.FLAG_CANCEL_CURRENT;

		Class<?> ancestorClass = clazz;

		while (ancestorClass != null) {
			ancestorClass = ancestorClass.getSuperclass();
			if (ancestorClass == Activity.class)
				return PendingIntent.getActivity(Process.context(), requestCode, intent, flags);
			else if (ancestorClass == BroadcastReceiver.class) {
				workaroundOsKillBug(intent);
				return PendingIntent.getBroadcast(Process.context(), requestCode, intent, flags);
			}
			else if (ancestorClass == Service.class)
				return PendingIntent.getService(Process.context(), requestCode, intent, flags);
		}

		throw new RuntimeException(clazz + " doesn't appear to be a component");
	}

	public PendingIntent makePending() {
		return makePending(makeIntent());
	}

	public Intent makeIntent() {
		Intent result = new Intent(Process.context(), clazz);
		workaroundOsKillBug(result);
		return result;
	}

	private void workaroundOsKillBug(Intent intent) {
		if (Build.VERSION.SDK_INT >= 16) {
			//Fixes bug in which process is killed when it receives a broadcast after task removal in Android 4.1 and above
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		}
	}

}
