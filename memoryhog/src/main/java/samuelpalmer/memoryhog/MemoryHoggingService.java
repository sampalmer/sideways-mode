package samuelpalmer.memoryhog;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class MemoryHoggingService extends Service {
	@SuppressWarnings("unused")
	private MemoryHog memoryHog;

	@Override
	public void onCreate() {
		super.onCreate();
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(android.R.drawable.sym_def_app_icon)
				.setContentTitle(getClass().getSimpleName())
				.getNotification();
		startForeground(getClass().getName().hashCode(), notification);

		memoryHog = new MemoryHog(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
