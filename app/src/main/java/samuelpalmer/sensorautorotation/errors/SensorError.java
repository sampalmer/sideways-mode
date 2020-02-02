package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.StringRes;

import org.json.JSONException;
import org.json.JSONObject;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DeviceUnsupportedActivity;

public class SensorError extends EnvironmentError {
    
    private static final String JSON_KEY_TYPE = "type";

    @StringRes
    private static int MESSAGE_RESOURCE = R.string.device_problem_explanation_sensor;
    
    SensorError() {}
    
    public enum Type {
        NOT_FOUND,
        NO_DATA
    }
    
    public SensorError(Context context, Type type) {
        summary = context.getText(R.string.device_problem);
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
    public String logMessage() {
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
    public PendingIntent showError(Context context) {
        Intent intent = new Intent(context, DeviceUnsupportedActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, MESSAGE_RESOURCE)
            .putExtra(DeviceUnsupportedActivity.EXTRA_IS_ERROR, true)
        ;
        
        return PendingIntent.getActivity(context, PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    @Override
    public CharSequence getMessage(Context context) {
        return context.getText(MESSAGE_RESOURCE);
    }

}
