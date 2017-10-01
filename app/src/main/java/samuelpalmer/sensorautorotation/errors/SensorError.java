package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DeviceUnsupportedActivity;

public class SensorError extends EnvironmentError {
	
	private static final String JSON_KEY_TYPE = "type";
	
	SensorError() {}
	
	public enum Type {
		NOT_FOUND,
		NO_DATA
	}
	
	public SensorError(Type type) {
		summary = CurrentProcess.context().getText(R.string.device_problem);
		this.type = type;
	}
	
	private Type type;
	
	@Override
	JSONObject serialiseDetails() throws JSONException {
		return new JSONObject().put(JSON_KEY_TYPE, type.name());
	}
	
	@Override
	void deserialiseDetails(JSONObject details) throws JSONException {
		String typeName = details.getString(JSON_KEY_TYPE);
		type = Type.valueOf(typeName);
	}
	
	@Override
	String logMessage() {
		return type.name();
	}
	
	@Override
	String code() {
		return "sensor";
	}
	
	@Override
	EnvironmentError newInstance() {
		return new SensorError();
	}
	
	@Override
	public PendingIntent showError() {
		Intent intent = new Intent(CurrentProcess.context(), DeviceUnsupportedActivity.class)
			.putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, R.string.device_problem_explanation_sensor);
		
		return PendingIntent.getActivity(CurrentProcess.context(), PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
	}
	
}
