package samuelpalmer.sensorautorotation.rules.deviceorientation;

import androidx.annotation.Nullable;

import org.junit.rules.ExternalResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.values.Orientation;

public abstract class DeviceOrientationRule extends ExternalResource {

    public static DeviceOrientationRule create() {
        // The first preference is emulator-based device orientation since we can watch the device
        // rotate, which makes it easy to develop and maintain tests.
        if (isEmulator())
            return new EmulatorDeviceOrientationRule();
        else
            return new MockDeviceOrientationRule();
    }

    private static boolean isEmulator() {
        // We only want to detect the official Android emulator since that's the one with
        // the special device orientation controls that we can use.
        //
        // Apparently some devices may contain this property but have it set to <c>0</c>, so it's
        // not enough to just check for the property's presence: https://stackoverflow.com/q/46643273/238753
        String qemuValue = getSystemProperty("ro.kernel.qemu");
        return qemuValue != null && qemuValue.equals("1");
    }

    private static String getSystemProperty(@SuppressWarnings("SameParameterValue") String name) {
        try {
            Class<?> systemPropertyClazz = Class.forName("android.os.SystemProperties");
            Method getSystemProperty = systemPropertyClazz.getMethod("get", String.class);
            return (String) getSystemProperty.invoke(systemPropertyClazz, name);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the device's physical orientation to the given value. Specify <c>null</c> for "unknown"
     * orientation (eg device flat on its back).
     */
    public abstract void setRotation(@Nullable Orientation deviceOrientation);

    DeviceOrientationRule() {}

}
