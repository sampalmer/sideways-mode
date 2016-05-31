package samuelpalmer.sensorautorotation.notifications;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {
	public static NotificationBuilderCompat create() {
		return NotificationBuilderCompat.create(Process.context())
			.setColor(ColourGetterCompat.getColor(Process.context(), R.color.primary_500))
			.setVisibility(NotificationBuilderCompat.VISIBILITY_PUBLIC)
		;
	}
}
