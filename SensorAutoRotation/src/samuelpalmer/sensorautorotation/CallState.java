package samuelpalmer.sensorautorotation;

import android.telephony.TelephonyManager;

public class CallState {
	
	private final int androidDeviceCallState;

	public CallState(int androidDeviceCallState) {
		this.androidDeviceCallState = androidDeviceCallState;
	}
	
	public CallState(String androidExtraDeviceCallState) {
		int result;
		
		if (androidExtraDeviceCallState.equals(TelephonyManager.EXTRA_STATE_IDLE))
			result = TelephonyManager.CALL_STATE_IDLE;
		else if (androidExtraDeviceCallState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
			result = TelephonyManager.CALL_STATE_OFFHOOK;
		else if (androidExtraDeviceCallState.equals(TelephonyManager.EXTRA_STATE_RINGING))
			result = TelephonyManager.CALL_STATE_RINGING;
		else
			throw new IllegalArgumentException("Unrecognised extra call state: " + androidExtraDeviceCallState);
		
		androidDeviceCallState = result;
	}
	
	public boolean possiblyOnCall() {
		return androidDeviceCallState != TelephonyManager.CALL_STATE_IDLE; //Because ringing could occur when on a call or off a call
	}
	
}
