package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class RecalibrationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean bound = bindService(new Intent(this, RotationService.class), serviceConnection, Context.BIND_ABOVE_CLIENT);

		if (!bound)
			finish();
	}

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			RotationService.LocalBinder binder = (RotationService.LocalBinder) service;
			binder.toggleRotation();
			unbindService(serviceConnection);
			finish();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

}
