package samuelpalmer.sensorautorotation.landscapetriggering;

import samuelpalmer.sensorautorotation.EnvironmentException;
import android.content.Context;

public class ProximitySensorMonitorFactory implements MonitorFactory {
	
	private Context context;

	public ProximitySensorMonitorFactory(Context context) {
		this.context = context;
	}
	
	@Override
	public Monitor create(Listener listener) throws EnvironmentException {
		return new ProximitySensorMonitor(context, listener, 1250);
	}

}
