package samuelpalmer.sensorautorotation.landscapetriggering;

import samuelpalmer.sensorautorotation.EnvironmentException;

public interface Listener {
	void trigger() throws EnvironmentException;
	void stop();
}
