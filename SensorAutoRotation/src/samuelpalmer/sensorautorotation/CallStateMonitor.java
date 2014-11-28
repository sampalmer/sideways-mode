package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallStateMonitor extends StateMonitor<Integer> {

	private Context context;
	private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			int result;
			
			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
				result = TelephonyManager.CALL_STATE_IDLE;
			else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
				result = TelephonyManager.CALL_STATE_OFFHOOK;
			else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING))
				result = TelephonyManager.CALL_STATE_RINGING;
			else {
				Log.w(CallStateMonitor.class.getName(), "Unrecognised extra call state: " + state);
				return;
			}
			
			CallStateMonitor.this.reportChange(result);
		}
	};

	public CallStateMonitor(Context context, Listener<Integer> listener) {
		super(listener);
		this.context = context;
	}
	
	@Override
	protected void startInternal() {
		context.registerReceiver(callStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
	}

	@Override
	protected Integer get() {
		TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephony.getCallState();
	}

	@Override
	protected void stop() throws EnvironmentException {
		context.unregisterReceiver(callStateReceiver);
	}

}
