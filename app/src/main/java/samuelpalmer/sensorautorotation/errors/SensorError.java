package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.StringRes;

import org.json.JSONException;
import org.json.JSONObject;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.MessageActivity;

/**
 * An error with the device's rotation-related sensors.
 */
public class SensorError extends EnvironmentError {
    
    @StringRes
    private static final int MESSAGE_RESOURCE = R.string.device_problem_explanation_sensor;
    
    public enum Type {
        NOT_FOUND,
        NO_DATA
    }

    private final Context context;

    /**
     * The type of sensor error.
     */
    private final Type type;

    public SensorError(Context context, Type type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public CharSequence getSummary() {
        return context.getText(R.string.device_problem);
    }

    @Override
    public CharSequence getMessage() {
        return context.getText(MESSAGE_RESOURCE);
    }

    @Override
    public String getLogMessage() {
        return type.name();
    }

    @Override
    public PendingIntent showError(Context context) {
        Intent intent = MessageActivity.makeIntent(
                context,
                R.drawable.image_error,
                context.getText(R.string.device_problem),
                context.getText(MESSAGE_RESOURCE),
                true
        )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ;

        return PendingIntent.getActivity(context, PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    static class Serialiser extends EnvironmentError.Serialiser<SensorError> {

        private static final String JSON_KEY_TYPE = "type";

        @Override
        public Class<SensorError> getTypeClass() {
            return SensorError.class;
        }

        @Override
        public JSONObject serialiseDetails(SensorError environmentError) throws JSONException {
            return new JSONObject()
                    .put(JSON_KEY_TYPE, environmentError.type.name());
        }

        @Override
        public SensorError deserialiseDetails(Context context, JSONObject serialised) throws JSONException {
            String typeName = serialised.getString(JSON_KEY_TYPE);
            Type type = Type.valueOf(typeName);
            return new SensorError(context, type);
        }

    }

}
