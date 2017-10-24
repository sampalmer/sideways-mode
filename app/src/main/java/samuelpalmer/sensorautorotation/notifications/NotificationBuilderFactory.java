package samuelpalmer.sensorautorotation.notifications;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {
	public static NotificationBuilderCompat create(Context context) {
		return NotificationBuilderCompat.create(context)
			.setColor(ContextCompat.getColor(context, R.color.primary_500))
			.setVisibility(NotificationBuilderCompat.VISIBILITY_PUBLIC)
		;
	}
}
