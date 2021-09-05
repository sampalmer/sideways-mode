package samuelpalmer.sensorautorotation.utilities;

/**
 * Represents a permission that is always granted during runtime
 */
public class AlwaysGrantedPermissionMonitor extends PermissionMonitor {

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    protected void subscribeToAdaptee() {}

    @Override
    protected void unsubscribeFromAdaptee() {}

}
