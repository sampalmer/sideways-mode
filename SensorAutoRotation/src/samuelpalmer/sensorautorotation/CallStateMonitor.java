package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class CallStateMonitor extends StateMonitor<CallState> {

	private Context context;
	private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			CallStateMonitor.this.reportChange(new CallState(state));
		}
	};

	public CallStateMonitor(Context context, Listener<CallState> listener) {
		super(listener);
		this.context = context;
	}
	
	@Override
	protected void startInternal() {
		context.registerReceiver(callStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
	}

	@Override
	protected CallState get() {
		TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return new CallState(telephony.getCallState());
	}

	@Override
	protected void stop() throws EnvironmentException {
		context.unregisterReceiver(callStateReceiver);
	}

}
