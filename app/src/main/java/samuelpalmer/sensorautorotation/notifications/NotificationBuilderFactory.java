package samuelpalmer.sensorautorotation.notifications;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {
	public static NotificationBuilderCompat create() {
		return NotificationBuilderCompat.create(CurrentProcess.context())
			.setColor(ColourGetterCompat.getColor(CurrentProcess.context(), R.color.primary_500))
			.setVisibility(NotificationBuilderCompat.VISIBILITY_PUBLIC)
		;
	}
}
