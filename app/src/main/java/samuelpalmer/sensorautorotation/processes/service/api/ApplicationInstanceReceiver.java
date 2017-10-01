package samuelpalmer.sensorautorotation.processes.service.api;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.BroadcastKillFix;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.processes.service.RotationService;

/**
 * Provides a way to communicate with {@link samuelpalmer.sensorautorotation.processes.service.ApplicationInstance} using broadcasts.
 */
public class ApplicationInstanceReceiver extends BroadcastReceiver {

	public static PendingIntent getPendingIntent(ApplicationInstanceCommand command, int requestCode) {
		Intent commandIntent = command.toIntent();

		//Want the intent to go to the broadcast receiver rather than the service itself
		commandIntent.setClass(CurrentProcess.context(), ApplicationInstanceReceiver.class);
		BroadcastKillFix.apply(commandIntent);
		
		return PendingIntent.getBroadcast(
			CurrentProcess.context(),
			requestCode,
			commandIntent,
			PendingIntentIds.PENDING_INTENT_FLAGS);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context, RotationService.class);
		ApplicationInstanceCommand command = ApplicationInstanceCommand.fromIntent(intent);
		ApplicationInstanceClient.send(command);
	}

}
