package samuelpalmer.sensorautorotation.processes.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

// Intentionally not inheriting from custom activity base classes since we don't want any extra behaviour for this activity since it's an error message intended to guide the user away from using the app.
public class MessageActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_EXPLANATION = "explanation";
    private static final String EXTRA_IS_ENVIRONMENT_ERROR = "is_error";

    private static final String STATE_CLEARED_ERROR = "cleared_error";

    private boolean clearedError = false;

    /**
     * @param title String resource id for activity title
     * @param explanation String resource id for explanation text
     * @param isEnvironmentError Whether the error is an {@link EnvironmentError}, in which case the error will be dismissed when the user arrives at this activity
     */
    public static Intent makeIntent(Context context, CharSequence title, CharSequence explanation, boolean isEnvironmentError) {
        return new Intent(context, MessageActivity.class)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_EXPLANATION, explanation)
                .putExtra(EXTRA_IS_ENVIRONMENT_ERROR, isEnvironmentError);
    }

    public static PendingIntent makePendingIntent(Context context, CharSequence title, CharSequence message, int requestCode, boolean isEnvironmentError) {
        // Opening the message over the top of the dashboard to give the user context that it's
        // about this app, and since it makes more sense to send the user here than to the settings
        // screen.
        Intent dashboardIntent = new Intent(context, DashboardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Not passing Intent.FLAG_ACTIVITY_NEW_TASK since the documentation for PendingIntent.getActivities() says to only supply that flag to the first activity in the array.
        Intent messageIntent = makeIntent(context, title, message, isEnvironmentError);

        Intent[] intents = {dashboardIntent, messageIntent};
        return PendingIntent.getActivities(context, requestCode, intents, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            throw new RuntimeException("No extras specified");

        CharSequence titleStringResource = getIntent().getCharSequenceExtra(EXTRA_TITLE);
        if (titleStringResource == null)
            throw new RuntimeException("Title not provided");

        setTitle(titleStringResource);
        setContentView(R.layout.activity_message);

        MaterialToolbar appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);

        CharSequence explanationStringResource = getIntent().getCharSequenceExtra(EXTRA_EXPLANATION);
        if (explanationStringResource == null)
            throw new RuntimeException("Explanation not provided");
        
        TextView explanationView = findViewById(R.id.message_explanation);
        explanationView.setText(explanationStringResource);

        if (savedInstanceState != null &&savedInstanceState.containsKey(STATE_CLEARED_ERROR))
            clearedError = savedInstanceState.getBoolean(STATE_CLEARED_ERROR);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        boolean isEnvironmentError = getIntent().getBooleanExtra(EXTRA_IS_ENVIRONMENT_ERROR, false);
        if (isEnvironmentError) {
            new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(this);

            // Remembering that we cleared the error so we don't clear any subsequent errors when
            // the user returns to this activity later on
            clearedError = true;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_CLEARED_ERROR, clearedError);
    }

}
