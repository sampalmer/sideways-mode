package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;

abstract class ServiceService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceProcess.ensureInitialised();
	}

	/**
	 * Works around a bug in which swiping away the UI makes Android kill this process immediately or
	 * later on (such as when it receives a broadcast).
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent) {
//		SystemServices.alarmManager().set(AlarmManager.RTC, System.currentTimeMillis(), new Intents(DummyActivity.class).makePending());

//		Intent intent = new Intent(this, EnsureAppLoadedReceiver.class).addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		SystemServices.alarmManager().set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);

//		Intent intent = new Intent(this, DummyActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
	}

}
