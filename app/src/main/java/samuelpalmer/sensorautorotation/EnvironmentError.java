package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class EnvironmentError {

	private static final String JSON_KEY_SUMMARY = "summary";
	private static final String JSON_KEY_MESSAGE = "message";

	private static final Settings.Setting<String> lastErrorSetting = ApplicationSettings.remote().lastErrorJson();
	public static final EventRegistrar onChange = lastErrorSetting;

	public static EnvironmentError lastError() {
		String serialised = lastErrorSetting.tryGet();
		if (serialised == null)
			return null;

		try {
			return new EnvironmentError(serialised);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public final CharSequence summary;
	public final CharSequence message;

	public EnvironmentError(int summaryResourceId, int messageResourceId, Object... messageArguments) {
		summary = StringResource.get(summaryResourceId);
		CharSequence messageEndingWithFullStop = StringResource.format(messageResourceId, messageArguments);
		message = StringResource.format(R.string.environment_error_message, messageEndingWithFullStop, R.string.app_name_full);
	}

	private EnvironmentError(String json) throws JSONException {
		JSONObject parsed = new JSONObject(json);
		summary = parsed.getString(JSON_KEY_SUMMARY);
		message = parsed.getString(JSON_KEY_MESSAGE);
	}

	private JSONObject serialise() {
		try {
			return new JSONObject()
					.put(JSON_KEY_SUMMARY, summary.toString())
					.put(JSON_KEY_MESSAGE, message.toString());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public void report() {
		//This is useful when checking log output from Cloud Test Lab on Google Play Store.
		Log.i(EnvironmentError.class.getSimpleName(), summary + ". " + message);

		postNotification();

		JSONObject json = serialise();
		lastErrorSetting.set(json.toString());

		new ServiceSynchroniser(ApplicationSettings.remote()).disableService();
	}

	private void postNotification() {
		NotificationBuilderCompat builder = NotificationBuilderFactory.create()
			.setSmallIcon(R.drawable.notification_error) //TODO: Maybe just use the app's normal icon so it's clear which app posted this notification
			.setContentIntent(showDialogIntent())
			.setContentTitle(summary)
			.setContentText(message)
			.setAutoCancel(true)
			.setTicker(message)
			.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
			.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
			.setChannel(NotificationChannels.ID_ERRORS);

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP)
			// Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
			// the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
			builder.setVibrate(new long[]{});

		Notification notification = builder.build();

		SystemServices.notificationManager().notify(NotificationIds.ERROR, notification);
	}

	public PendingIntent showDialogIntent() {
		return MessageDialogActivity.makePendingIntent(summary, message, PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE);
	}

	public static void clear() {
		SystemServices.notificationManager().cancel(NotificationIds.ERROR);
		lastErrorSetting.remove();
	}

}