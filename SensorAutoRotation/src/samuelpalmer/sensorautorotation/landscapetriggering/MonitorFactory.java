package samuelpalmer.sensorautorotation.landscapetriggering;

import samuelpalmer.sensorautorotation.EnvironmentException;

public interface MonitorFactory {
	Monitor create(Listener listener) throws EnvironmentException;
}
