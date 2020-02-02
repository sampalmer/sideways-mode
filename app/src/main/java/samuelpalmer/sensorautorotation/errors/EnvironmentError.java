package samuelpalmer.sensorautorotation.errors;

import android.app.PendingIntent;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An error that is outside of the control of the app. This is typically a problem caused by the
 * user, device, operating system, or other app.
 */
@SuppressWarnings("WeakerAccess")
public abstract class EnvironmentError {

    /**
     * The {@link Class#getSimpleName()} of the subclass of {@link EnvironmentError} that this error
     * represents
     */
    private static final String JSON_KEY_CLASS_NAME = "className";
    private static final String JSON_KEY_DETAILS = "details";

    private static final EnvironmentError.Serialiser[] serialisers = new EnvironmentError.Serialiser[] {
            new GenericError.Serialiser(),
            new SensorError.Serialiser()
    };

    public String toJson() {
        String className = getClass().getSimpleName();
        Serialiser serialiser = getSerialiser(className);

        try {
            return new JSONObject()
                    .put(JSON_KEY_CLASS_NAME, serialiser.getTypeClass().getSimpleName())
                    .put(JSON_KEY_DETAILS, serialiser.serialiseDetails(this))
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static EnvironmentError fromJson(Context context, String json) {
        try {
            JSONObject parsed = new JSONObject(json);
            String className = parsed.getString(JSON_KEY_CLASS_NAME);
            JSONObject details = parsed.getJSONObject(JSON_KEY_DETAILS);

            Serialiser serialiser = getSerialiser(className);
            return serialiser.deserialiseDetails(context, details);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Serialiser getSerialiser(String className) {
        for (EnvironmentError.Serialiser serialiser : serialisers)
            if (serialiser.getTypeClass().getSimpleName().equals(className))
                return serialiser;

        throw new RuntimeException("Didn't find a serialiser for class name " + className);
    }

    public abstract CharSequence getSummary();
    public abstract CharSequence getMessage();

    /**
     * Gets a description of the error suitable to be logged to the device log instead of displayed
     * to the user. This is useful for troubleshooting.
     */
    public abstract String getLogMessage();

    public abstract PendingIntent showError(Context context);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EnvironmentError
            &&
            toJson().equals(((EnvironmentError)obj).toJson());
    }

    @Override
    public int hashCode() {
        return toJson().hashCode();
    }

    /**
     * Every subclass of {@link EnvironmentError} must implement this.
     */
    static abstract class Serialiser<T extends EnvironmentError> {

        /**
         * Gets the class that this serialiser supports.
         */
        public abstract Class<T> getTypeClass();

        public abstract JSONObject serialiseDetails(T environmentError) throws JSONException;
        public abstract T deserialiseDetails(Context context, JSONObject serialised) throws JSONException;

    }
    
}