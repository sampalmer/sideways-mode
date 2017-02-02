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

	// Always cancelling the existing pending intent since it's possible for the existing one to die and become permanently useless.
	public static final int PENDING_INTENT_FLAGS = PendingIntent.FLAG_CANCEL_CURRENT;

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

	public abstract PendingIntent pendingIntent(Intent intent, int requestCode);

	public Intent intent() {
		return new Intent(CurrentProcess.context(), clazz);
	}

	public PendingIntent pendingIntent(int requestCode) {
		return pendingIntent(intent(), requestCode);
	}

	private static class ActivityComponentType extends ComponentType {
		@Override
		public PendingIntent pendingIntent(Intent intent, int requestCode) {
			//TODO: Maybe always add FLAG_NEW_TASK, as required according to the getActivity() documentation
			return PendingIntent.getActivity(CurrentProcess.context(), requestCode, intent, PENDING_INTENT_FLAGS);
		}
	}

	private static class BroadcastReceiverComponentType extends ComponentType {
		@Override
		public PendingIntent pendingIntent(Intent intent, int requestCode) {
			BroadcastKillFix.apply(intent);
			return PendingIntent.getBroadcast(CurrentProcess.context(), requestCode, intent, PENDING_INTENT_FLAGS);
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
		public PendingIntent pendingIntent(Intent intent, int requestCode) {
			return PendingIntent.getService(CurrentProcess.context(), requestCode, intent, PENDING_INTENT_FLAGS);
		}
	}
}
