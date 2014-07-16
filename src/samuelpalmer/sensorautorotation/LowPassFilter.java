package samuelpalmer.sensorautorotation;

import android.hardware.SensorEvent;

public class LowPassFilter {
	private static final float SMOOTHING_TIME_MILLISECONDS = 200f;

	private float[] lastData;
	private long lastDataReadTimeNanoseconds;
	
	public float[] process(SensorEvent data) {
		long nowNanoseconds = data.timestamp;
		
        if (lastData == null) {
        	lastData = new float[data.values.length];
        	lastDataReadTimeNanoseconds = data.timestamp;
        }
        
    	if (data.values.length != lastData.length)
    		throw new RuntimeException("Expected " + lastData.length + " components but instead found " + data.values.length);
    	
		// alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
    	float timeElapsedNanoseconds = nowNanoseconds - lastDataReadTimeNanoseconds;
        final float alpha = SMOOTHING_TIME_MILLISECONDS / (SMOOTHING_TIME_MILLISECONDS + timeElapsedNanoseconds);
    	
    	for (int i = 0; i < data.values.length; ++i)
    		lastData[i] = alpha * lastData[i] + (1 - alpha) * data.values[i];
    	lastDataReadTimeNanoseconds = nowNanoseconds;
    	
    	return lastData.clone();
	}
}
