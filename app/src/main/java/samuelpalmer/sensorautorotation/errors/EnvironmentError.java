package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("WeakerAccess")
public abstract class EnvironmentError {
    
    private static final String JSON_KEY_SUMMARY = "summary";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_DETAILS = "details";
    
    public static EnvironmentError fromJson(String json) {
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
            throw new IllegalArgumentException(e);
        }
    }
    
    public String toJson() {
        try {
            return new JSONObject()
                .put(JSON_KEY_SUMMARY, summary.toString())
                .put(JSON_KEY_TYPE, code())
                .put(JSON_KEY_DETAILS, serialiseDetails())
                .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    CharSequence summary;
    
    EnvironmentError() {}
    
    abstract JSONObject serialiseDetails() throws JSONException;
    abstract void deserialiseDetails(JSONObject details) throws JSONException;
    public abstract String logMessage();
    abstract String code();
    abstract EnvironmentError newInstance();
    
    public abstract PendingIntent showError(Context context);
    
    public CharSequence getSummary() {
        return summary;
    }

    public abstract CharSequence getMessage(Context context);

    @Override
    public boolean equals(Object obj) {
        return
            obj instanceof EnvironmentError
            &&
            this.toJson().equals(((EnvironmentError)obj).toJson());
    }
    
    @Override
    public int hashCode() {
        return toJson().hashCode();
    }
    
}