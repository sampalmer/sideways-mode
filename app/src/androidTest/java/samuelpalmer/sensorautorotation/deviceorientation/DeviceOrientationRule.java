package samuelpalmer.sensorautorotation.deviceorientation;

import org.junit.rules.ExternalResource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        // the special device orientation controls that we can use
        String qemuValue = getSystemProperty("ro.kernel.qemu");
        return qemuValue != null && qemuValue.equals("1");
    }

    private static String getSystemProperty(String name) {
        try {
            Class<?> systemPropertyClazz = Class.forName("android.os.SystemProperties");
            Method getSystemProperty = systemPropertyClazz.getMethod("get", String.class);
            return (String) getSystemProperty.invoke(systemPropertyClazz, name);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void setUpright();
    public abstract void setRightSide();
    public abstract void setUpsideDown();
    public abstract void setLeftSide();
    public abstract void setUnknown();

}
