package samuelpalmer.sensorautorotation.utilities;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Keeps track of whether a permission is granted
 */
public abstract class PermissionMonitor extends EventRegistrarAdaptor {

    /**
     * Whether the permission is currently granted
     */
    public abstract boolean hasPermission();

}
