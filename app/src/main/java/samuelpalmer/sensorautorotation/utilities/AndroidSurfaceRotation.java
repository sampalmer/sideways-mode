package samuelpalmer.sensorautorotation.utilities;

import android.view.Surface;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Utilities for working with Android's {@link Surface}{@code .ROTATION_} constants.
 */
public abstract class AndroidSurfaceRotation {
    
    /**
     * Converts the given rotation to an {@link Orientation}, assuming that that the rotation
     * is clockwise.
     */
    public static Orientation toOrientationClockwise(int clockwiseRotation) throws IllegalArgumentException {
        int offset = offset(clockwiseRotation);
        return Orientation.fromOffset(offset);
    }
    
    /**
     * Converts the given rotation to an {@link Orientation}, assuming that that the rotation
     * is anticlockwise.
     */
    public static Orientation toOrientationAnticlockwise(int anticlockwiseRotation) throws IllegalArgumentException {
        int anticlockwiseOffset = offset(anticlockwiseRotation);
        int clockwiseOffset = NumberUtilities.wrap(4 - anticlockwiseOffset, 4);
        return Orientation.fromOffset(clockwiseOffset);
    }
    
    /**
     * Converts the given orientation to a clockwise rotation.
     */
    public static int fromOrientationClockwise(Orientation orientation) {
        return Surface.ROTATION_0 + orientation.offset;
    }
    
    @SuppressWarnings("unused")
    public static String name(int rotation) throws IllegalArgumentException {
        return "ROTATION_" + offset(rotation) * 90;
    }
    
    private static int offset(int rotation) throws IllegalArgumentException {
        if (rotation < Surface.ROTATION_0 || rotation > Surface.ROTATION_270)
            throw new IllegalArgumentException("Value " + rotation + " is not a valid surface rotation");
        
        return rotation - Surface.ROTATION_0;
    }
    
}
