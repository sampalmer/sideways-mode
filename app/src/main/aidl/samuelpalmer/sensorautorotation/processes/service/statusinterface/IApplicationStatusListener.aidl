// IApplicationStatusListener.aidl
package samuelpalmer.sensorautorotation.processes.service.statusinterface;

import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;

interface IApplicationStatusListener {
    void onChange(in ApplicationStatus status);
}
