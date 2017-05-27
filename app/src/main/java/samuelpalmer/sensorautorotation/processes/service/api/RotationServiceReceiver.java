package samuelpalmer.sensorautorotation.processes.service.api;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.processes.service.RotationService;

/**
 * Provides a way to communicate with RotationService using broadcasts.
 */
public class RotationServiceReceiver extends BroadcastReceiver {

	public static PendingIntent getPendingIntent(RotationServiceCommand command, int requestCode) {
		ComponentType componentType = ComponentType.create(RotationServiceReceiver.class);
		Intent commandIntent = command.toIntent();

		//Want the intent to go to the broadcast receiver rather than the service itself
		commandIntent.setClass(CurrentProcess.context(), RotationServiceReceiver.class);
		return componentType.pendingIntent(commandIntent, requestCode);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context, RotationService.class);
		RotationServiceCommand command = RotationServiceCommand.fromIntent(intent);
		RotationServiceClient.send(command);
	}

}
