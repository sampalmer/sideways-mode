package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.DeviceUnsupportedActivity;

public class SensorError extends EnvironmentError {
	
	private static final String JSON_KEY_TYPE = "type";
	
	SensorError() {}
	
	public enum Type {
		NOT_FOUND,
		NO_DATA
	}
	
	public SensorError(Type type) {
		summary = StringResource.get(R.string.device_problem);
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
		return ComponentType.create(DeviceUnsupportedActivity.class).pendingIntent(PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE);
	}
	
}
