package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

/**
 * Override mode to use to bypass the device's physical orientation sensors.
 * For testing purposes only.
 */
public enum SensorOverride {

    /** Turn off sensor override */
    OFF,

    /** Set sensor to "unknown" orientation (eg when device is flat) */
    UNKNOWN,

    /** Set sensor to device in upright position */
    UPRIGHT,

    /** Set sensor to device on its left side */
    LEFT_SIDE,

    /** Set sensor to device on its right side */
    RIGHT_SIDE,

    /** Set sensor to device upside-down */
    UPSIDE_DOWN,

}
