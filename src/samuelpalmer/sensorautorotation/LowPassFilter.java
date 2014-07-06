package samuelpalmer.sensorautorotation;

import java.util.Date;

public class LowPassFilter {
	private static final float TIME_CONSTANT = 200f;

	private final Clock clock;
	
	private float[] lastData;
	private Date lastDataRead;
	
	public LowPassFilter(Clock clock) {
		this.clock = clock;
	}
	
	public float[] process(float[] data) {
		Date time = clock.read();
		
        if (lastData == null) {
        	lastData = new float[data.length];
        	lastDataRead = time;
        }
        
    	if (data.length != lastData.length)
    		throw new RuntimeException("Expected " + lastData.length + " components but instead found " + data.length);
    	
		// alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
    	float dt = (time.getTime() - lastDataRead.getTime());
        final float alpha = TIME_CONSTANT / (TIME_CONSTANT + dt);
    	
    	for (int i = 0; i < data.length; ++i)
    		lastData[i] = alpha * lastData[i] + (1 - alpha) * data[i];
        lastDataRead = time;
    	
    	return lastData.clone();
	}
}
