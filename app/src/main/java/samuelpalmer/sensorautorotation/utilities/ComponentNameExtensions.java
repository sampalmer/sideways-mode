package samuelpalmer.sensorautorotation.utilities;

import android.content.ComponentName;
import android.content.pm.PackageItemInfo;

@SuppressWarnings("WeakerAccess")
public final class ComponentNameExtensions {
	//Intents may resolve to this when run on the official Android emulators if they doesn't match anything.
	public static final ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
	
	public static ComponentName makeFrom(PackageItemInfo packageItem) {
		return makeRelative(packageItem.packageName, packageItem.name);
	}
	
	public static ComponentName makeRelative(String packageName, String className) {
		boolean isRelativeClassName = className.startsWith(".");
		String fullClassName = isRelativeClassName ? packageName + className : className;
		return new ComponentName(packageName, fullClassName);
	}
}
