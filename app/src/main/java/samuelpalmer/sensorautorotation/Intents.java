package samuelpalmer.sensorautorotation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Intents {

	private Context context;

	public Intents(Context context) {
		this.context = context;
	}

	public PendingIntent makePending(Intent intent) {
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public Intent makeIntent(Class<?> clazz) {
		return new Intent(context, clazz);
	}

}
