package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;

public class GenericError extends EnvironmentError {
	
	private static final String JSON_KEY_MESSAGE = "message";
	
	private CharSequence message;
	
	public GenericError(Context context, int summaryResourceId, CharSequence messageEndingWithFullStop) {
		summary = context.getText(summaryResourceId);
		message = context.getString(R.string.environment_error_message, messageEndingWithFullStop, context.getText(R.string.app_name_full));
	}
	
	GenericError() {}
	
	@Override
	JSONObject serialiseDetails() throws JSONException {
		return new JSONObject().put(JSON_KEY_MESSAGE, message);
	}
	
	@Override
	void deserialiseDetails(JSONObject details) throws JSONException {
		message = details.getString(JSON_KEY_MESSAGE);
	}
	
	@Override
	public String logMessage() {
		return message.toString();
	}
	
	@Override
	String code() {
		return "generic";
	}
	
	@Override
	EnvironmentError newInstance() {
		return new GenericError();
	}
	
	@Override
	public PendingIntent showError(Context context) {
		return MessageDialogActivity.makePendingIntent(context, summary, message, PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE, true);
	}
	
}
