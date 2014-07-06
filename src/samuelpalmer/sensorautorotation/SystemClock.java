package samuelpalmer.sensorautorotation;

import java.util.Date;

public class SystemClock implements Clock {

	@Override
	public Date read() {
		return new Date();
	}

}
