package samuelpalmer.sensorautorotation.utilities;

import android.content.ComponentName;

/**
 * Utilities for working with {@link ComponentName}s
 */
@SuppressWarnings("WeakerAccess")
public final class ComponentNameExtensions {
    // Intents may resolve to this when run on the official Android emulators if they doesn't match anything.
    public static final ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");

    public static ComponentName makeRelative(String packageName, String className) {
        boolean isRelativeClassName = className.startsWith(".");
        String fullClassName = isRelativeClassName ? packageName + className : className;
        return new ComponentName(packageName, fullClassName);
    }
}
