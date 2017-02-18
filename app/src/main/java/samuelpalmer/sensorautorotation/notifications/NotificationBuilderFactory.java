package samuelpalmer.sensorautorotation.notifications;

import android.support.v4.content.ContextCompat;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {
	public static NotificationBuilderCompat create() {
		return NotificationBuilderCompat.create(CurrentProcess.context())
			.setColor(ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500))
			.setVisibility(NotificationBuilderCompat.VISIBILITY_PUBLIC)
		;
	}
}
