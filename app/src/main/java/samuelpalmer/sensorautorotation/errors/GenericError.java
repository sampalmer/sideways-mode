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
 * An {@link EnvironmentError} that is shown to the user in a {@link MessageActivity}.
 */
public class GenericError extends EnvironmentError {
    
    private final CharSequence summary;
    private final CharSequence message;

    public GenericError(Context context, @StringRes int summaryResourceId, CharSequence messageEndingWithFullStop) {
        this(
                context.getText(summaryResourceId),
                context.getString(R.string.environment_error_message, messageEndingWithFullStop, context.getText(R.string.app_name_full))
        );
    }
    
    private GenericError(CharSequence summary, CharSequence message) {
        this.summary = summary;
        this.message = message;
    }
    
    @Override
    public CharSequence getSummary() {
        return summary;
    }

    @Override
    public CharSequence getMessage() {
        return message;
    }

    @Override
    public String getLogMessage() {
        return message.toString();
    }

    @Override
    public PendingIntent showError(Context context) {
        Intent intent = MessageActivity.makeIntent(context, R.drawable.image_error, summary, message, true);
        return MessageActivity.makePendingIntent(context, intent, PendingIntentIds.ENVIRONMENT_ERROR_MESSAGE);
    }

    static class Serialiser extends EnvironmentError.Serialiser<GenericError> {

        private static final String JSON_KEY_SUMMARY = "summary";
        private static final String JSON_KEY_MESSAGE = "message";

        @Override
        public Class<GenericError> getTypeClass() {
            return GenericError.class;
        }

        @Override
        public JSONObject serialiseDetails(GenericError environmentError) throws JSONException {
            return new JSONObject()
                    .put(JSON_KEY_SUMMARY, environmentError.summary)
                    .put(JSON_KEY_MESSAGE, environmentError.message);
        }

        @Override
        public GenericError deserialiseDetails(Context context, JSONObject serialised) throws JSONException {
            CharSequence summary = serialised.getString(JSON_KEY_SUMMARY);
            CharSequence message = serialised.getString(JSON_KEY_MESSAGE);
            return new GenericError(summary, message);
        }

    }

}
