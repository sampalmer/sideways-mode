package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import androidx.core.app.NotificationCompat;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.testing.FieldConstants;

import java.util.Collections;
import java.util.Set;

public class IncompatibleAppsSuggester extends State {
	
	private final Context context;
	private final ActivityMonitor activityMonitor;
	private final SharedPreferences preferences;
	private final OverriddenOrientationDetector overriddenOrientationDetector;
	private final SupportedOrientationsDetector supportedOrientationsDetector;
	private final NotificationManager notificationManager;
	
	private Set<String> incompatibleAppPackages;
	
	public IncompatibleAppsSuggester(Context context, ActivityMonitor activityMonitor, ScreenRotationMonitor screenRotationMonitor, LockScreenMonitor lockScreenMonitor) {
		this.context = context;
		this.activityMonitor = activityMonitor;
		preferences = ServiceSettings.getSharedPreferences(context);
		overriddenOrientationDetector = new OverriddenOrientationDetector(context, screenRotationMonitor, lockScreenMonitor);
		supportedOrientationsDetector = new SupportedOrientationsDetector(context);
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	protected void start() {
		preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
		activityMonitor.subscribe(activityChanged);
		overriddenOrientationDetector.subscribe(anythingChanged);
		supportedOrientationsDetector.subscribe(anythingChanged);
		
		loadIncompatibleAppPackages();
		
		check();
	}
	
	@Override
	protected void stop() {
		supportedOrientationsDetector.unsubscribe(anythingChanged);
		overriddenOrientationDetector.unsubscribe(anythingChanged);
		activityMonitor.unsubscribe(activityChanged);
		preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
	}
	
	private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
		if (isStarted() && key.equals(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES)) {
			loadIncompatibleAppPackages();
			check();
			cancelObsoleteNotifications();
		}
	};
	
	private void loadIncompatibleAppPackages() {
		incompatibleAppPackages = preferences.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
	}
	
	private final EventHandler activityChanged = new EventHandler() {
		@Override
		public void update() {
			isCurrentActivityADialog(); // TODO: Remove this!
			overriddenOrientationDetector.reset();
			supportedOrientationsDetector.reset();
			check();
		}
	};
	
	private final EventHandler anythingChanged = this::check;
	
	private void check() {
		ComponentName currentActivity = activityMonitor.currentOrNull();

		if (
			currentActivity != null // We can't do anything if we don't know which activity is open
			&&
			!incompatibleAppPackages.contains(currentActivity.getPackageName()) // No point prompting if the app is already marked as incompatible
			&&
			overriddenOrientationDetector.overridesOrientation()
			&&
			supportedOrientationsDetector.supportsLandscape() && supportedOrientationsDetector.supportsPortrait() // Apps that are locked in the one orientation aren't a problem
			&&
			!currentActivity.getPackageName().equals(context.getPackageName()) // Sideways Mode might looks like it's incompatible if it's open over the top of an incompatible app
			// TODO: Don't prompt if a notification already exists for this app
			// TODO: Don't prompt if the user has asked not to be prompted again for this app
			// TODO: Don't prompt if the user has already been asked and dismissed the notification
			// TODO: Don't prompt if the current activity orientation is controlled by the activity behind it (eg a dialog)
			// TODO: For some reason a suggestion was made to mark the launcher as incompatible when I opened YouTube. This might be a timing-related issue caused by the accessibility service. If that's the case, then the issue no longer applies since the accessibility service has been replaced with polling.
		) {
			PackageManager packageManager = context.getPackageManager();
			
			try {
				// TODO: We might have lost track of what the real current activity is. For example, the user might have opened the recents screen and returned to an app, but we might have only been notified about the recents screen.
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(currentActivity.getPackageName(), 0);
				Intent intent = MarkAppAsIncompatibleReceiver.makeIntent(context, currentActivity.getPackageName());
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, PendingIntentIds.INCOMPATIBLE_APPS_SUGGESTION, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
				String summary = "Is " + applicationInfo.loadLabel(packageManager) + " incompatible?";
				
				// TODO: Stack/group multiple of these notifications so you get one for each incompatible app?
				NotificationBuilderCompat builder = NotificationBuilderFactory
					.create(context)
					.setSmallIcon(R.drawable.notification_rotation)
					.setContentTitle(summary)
					.setContentText("Tap to fix")
					.setContentIntent(pendingIntent)
					.setChannelId(NotificationChannels.ID_INCOMPATIBLE)
//					.setDeleteIntent() // TODO: Don't ask again if user dismisses it?
					.setTicker(summary) //Helps ensure the user actually notices notification. Only works on Android 4.
					.setPriority(NotificationCompat.PRIORITY_HIGH)
//					.addAction() // TODO: Provide button to not ask again? Not sure if this works on API 14
					;
				
				// TODO: Add a sound and maybe a vibration since the user might be in the middle of a full screen video or something
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
					// Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
					// the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
					builder.setVibrate(new long[]{});

				notificationManager.notify(currentActivity.getPackageName(), NotificationIds.INCOMPATIBLE_APP, builder.build());
			} catch (PackageManager.NameNotFoundException e) {
				Log.w(getClass().getSimpleName(), e); // Don't really need to worry about this; maybe the app got uninstalled or something
			}
		}
	}

	private void cancelObsoleteNotifications() {
	    // The user might have added a suggested app to the incompatible apps list,
        // so we'd better cancel the corresponding notification.
	    for (String incompatibleAppPackage : incompatibleAppPackages)
	        notificationManager.cancel(incompatibleAppPackage, NotificationIds.INCOMPATIBLE_APP);
    }

    private boolean isCurrentActivityADialog() {
		ComponentName currentActivity = activityMonitor.currentOrNull();

		if (currentActivity == null)
			return false;

		Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "Checking activity: " + currentActivity.flattenToShortString());

		PackageManager packageManager = context.getPackageManager();
		try {
			ActivityInfo activityInfo = packageManager.getActivityInfo(currentActivity, 0);
			String orientation = new FieldConstants(ActivityInfo.class, "SCREEN_ORIENTATION_").getName(activityInfo.screenOrientation);
			Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "Activity orientation: " + orientation);

			int activityThemeResource = activityInfo.getThemeResource();

			TypedArray typedArray = context.obtainStyledAttributes(activityThemeResource, new int[]{android.R.attr.windowIsFloating});
			try {
				boolean windowIsFloatingHasValue = typedArray.hasValue(0);
				String windowIsFloating = typedArray.getString(0);
				Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "windowIsFloating: " + windowIsFloating + ", has value: " + windowIsFloatingHasValue);
			}
			finally {
				typedArray.recycle();
			}

			typedArray = context.obtainStyledAttributes(activityThemeResource, new int[]{android.R.attr.windowIsTranslucent});
			try {
				boolean windowIsTranslucentHasValue = typedArray.hasValue(0);
				String windowIsTranslucent = typedArray.getString(0);
				Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "windowIsTranslucent: " + windowIsTranslucent + ", has value: " + windowIsTranslucentHasValue);
			}
			finally {
				typedArray.recycle();
			}

			typedArray = context.obtainStyledAttributes(activityThemeResource, new int[]{android.R.attr.windowBackground});
			try {
				TypedValue value = new TypedValue();
				if (!typedArray.getValue(0, value))
					throw new RuntimeException("Couldn't read value");

				Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "windowBackground: " + value + ", type: " + new FieldConstants(TypedValue.class, "TYPE_").getName(value.type));
			}
			finally {
				typedArray.recycle();
			}


//			Resources activityResources = packageManager.getResourcesForActivity(currentActivity);
//			activityResources.


			Resources resources = packageManager.getResourcesForApplication(currentActivity.getPackageName());
			TypedValue themeValue = new TypedValue();
			resources.getValue(activityThemeResource, themeValue, true);
			Log.i(IncompatibleAppsSuggester.class.getSimpleName(), "Theme: " + themeValue);

			// TODO: Figure out if the activity's theme contains "windowIsFloating"
			/* Or maybe one of these:
			        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowIsTranslucent">true</item>
			 */

		} catch (PackageManager.NameNotFoundException e) {
			Log.w(IncompatibleAppsSuggester.class.getSimpleName(), "Couldn't find info for current activity");
			return false;
		}

		return false; // TODO: Do this properly!
	}

}
