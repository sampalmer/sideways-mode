// IServiceStatusListener.aidl
package samuelpalmer.sensorautorotation.processes.service.statusinterface;

import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;

interface IServiceStatusListener {
    void onChange(in ServiceStatus status);
}
