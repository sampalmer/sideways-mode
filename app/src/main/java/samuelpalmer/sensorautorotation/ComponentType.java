package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;

/**
 * Should be used for creation of all intents since this works around an Android bug.
 */
public abstract class ComponentType {

	private static final int requestCode = 0;

	// Using FLAG_CANCEL_CURRENT fixes rare issue where pending intent permanently stops working
	// http://stackoverflow.com/q/21250364/238753
	// Note that on 2015-07-28 I made a commit to *stop* using this flag saying doing so would fix this same issue.
	// If it comes back, maybe we should try a different technique such as randomised request codes or calling .cancel() on the pending intent each time before using it.
	private static final int flags = PendingIntent.FLAG_CANCEL_CURRENT;

	public static ComponentType create(Class<?> clazz) {
		Class<?> ancestorClass = clazz;

		while (ancestorClass != null) {
			ancestorClass = ancestorClass.getSuperclass();

			ComponentType componentType = lookup(ancestorClass);
			if (componentType != null) {
				componentType.clazz = clazz;
				return componentType;
			}
		}

		throw new RuntimeException(clazz + " doesn't appear to be a component");
	}

	private static ComponentType lookup(Class<?> ancestorClass) {
		if (ancestorClass == Activity.class)
			return new ActivityComponentType();
		else if (ancestorClass == BroadcastReceiver.class)
			return new BroadcastReceiverComponentType();
		else if (ancestorClass == Service.class)
			return new ServiceComponentType();
		else
			return null;
	}

	private Class<?> clazz;

	public abstract PendingIntent pendingIntent(Intent intent);

	public Intent intent() {
		return new Intent(Process.context(), clazz);
	}

	public PendingIntent pendingIntent() {
		return pendingIntent(intent());
	}

	private static class ActivityComponentType extends ComponentType {
		@Override
		public PendingIntent pendingIntent(Intent intent) {
			//TODO: Maybe always add FLAG_NEW_TASK, as required according to the getActivity() documentation
			return PendingIntent.getActivity(Process.context(), requestCode, intent, flags);
		}
	}

	private static class BroadcastReceiverComponentType extends ComponentType {
		@Override
		public PendingIntent pendingIntent(Intent intent) {
			BroadcastKillFix.apply(intent);
			return PendingIntent.getBroadcast(Process.context(), requestCode, intent, flags);
		}

		@Override
		public Intent intent() {
			Intent result = super.intent();
			BroadcastKillFix.apply(result);
			return result;
		}

	}

	private static class ServiceComponentType extends ComponentType {
		@Override
		public PendingIntent pendingIntent(Intent intent) {
			return PendingIntent.getService(Process.context(), requestCode, intent, flags);
		}
	}
}
