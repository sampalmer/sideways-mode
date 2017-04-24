package samuelpalmer.sensorautorotation.processes.directboot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

@RequiresApi(24)
public class DirectBootService extends Service {
	
	private boolean registered;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (DirectBootReceiver.syncServiceIfUserUnlocked(this)) {
			stopSelf();
			return;
		}
		
		registerReceiver(onUserUnlocked, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
		registered = true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (registered) {
			unregisterReceiver(onUserUnlocked);
			registered = false;
		}
	}
	
	private final BroadcastReceiver onUserUnlocked = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			DirectBootReceiver.syncService();
			stopSelf();
		}
	};
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
