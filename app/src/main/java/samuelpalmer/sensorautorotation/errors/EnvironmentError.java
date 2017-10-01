package samuelpalmer.sensorautorotation.errors;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("WeakerAccess")
public abstract class EnvironmentError {
	
	private static final String JSON_KEY_SUMMARY = "summary";
	private static final String JSON_KEY_TYPE = "type";
	private static final String JSON_KEY_DETAILS = "details";
	
	private static final Settings.Setting<String> lastErrorSetting = ServiceSettings.instance().lastErrorJson();
	public static final EventRegistrar onChange = lastErrorSetting;
	
	public static EnvironmentError lastError() {
		String json = lastErrorSetting.tryGet();
		if (json == null)
			return null;
		
		try {
			JSONObject parsed = new JSONObject(json);
			String summary = parsed.getString(JSON_KEY_SUMMARY);
			String type = parsed.getString(JSON_KEY_TYPE);
			JSONObject details = parsed.getJSONObject(JSON_KEY_DETAILS);
			
			EnvironmentError result = ErrorType.newInstance(type);
			result.summary = summary;
			result.deserialiseDetails(details);
			
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private JSONObject serialise() {
		try {
			return new JSONObject()
				.put(JSON_KEY_SUMMARY, summary.toString())
				.put(JSON_KEY_TYPE, code())
				.put(JSON_KEY_DETAILS, serialiseDetails());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	CharSequence summary;
	
	EnvironmentError() {}
	
	public void report() {
		//This is useful when checking log output from Cloud Test Lab on Google Play Store.
		Log.i(EnvironmentError.class.getSimpleName(), summary + ". " + getClass().getSimpleName() + ": " + logMessage());

		postNotification();

		JSONObject json = serialise();
		lastErrorSetting.set(json.toString());
		
		ServiceSynchroniser.disableService();
	}
	
	abstract JSONObject serialiseDetails() throws JSONException;
	abstract void deserialiseDetails(JSONObject details) throws JSONException;
	abstract String logMessage();
	abstract String code();
	abstract EnvironmentError newInstance();
	
	private void postNotification() {
		NotificationBuilderCompat builder = NotificationBuilderFactory.create()
			.setSmallIcon(R.drawable.notification_error) //TODO: Maybe just use the app's normal icon so it's clear which app posted this notification
			.setContentIntent(showError())
			.setContentTitle(summary)
			.setContentText(CurrentProcess.context().getText(R.string.app_disabled_view_error))
			.setAutoCancel(true)
			.setTicker(summary) //Helps ensure the user actually notices that the error occurred. Only works on Android 4.
			.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
			.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
			.setChannelId(NotificationChannels.ID_ERRORS);

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
			// Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
			// the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
			builder.setVibrate(new long[]{});

		Notification notification = builder.build();
		
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).notify(NotificationIds.ERROR, notification);
	}

	public abstract PendingIntent showError();

	public static void clear() {
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationIds.ERROR);
		lastErrorSetting.remove();
	}
	
	public CharSequence getSummary() {
		return summary;
	}
}