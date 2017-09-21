package samuelpalmer.sensorautorotation.notifications;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;

public class NotificationBuilderFactory {
	public static NotificationCompat.Builder create(@NonNull String channelId) {
		return new NotificationCompat.Builder(CurrentProcess.context(), channelId)
			.setColor(ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500))
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
		;
	}
}
