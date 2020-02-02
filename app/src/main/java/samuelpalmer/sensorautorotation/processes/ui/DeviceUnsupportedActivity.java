package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

// Intentionally not inheriting from custom activity base classes since we don't want any extra behaviour for this activity since it's an error message intended to guide the user away from using the app.
public class DeviceUnsupportedActivity extends AppCompatActivity {
    
    private static final String EXTRA_EXPLANATION = "explanation";
    private static final String EXTRA_IS_ERROR = "is_error";

    /**
     * @param explanation String resource id for explanation text.
     * @param isError Whether the error is an {@link EnvironmentError}, in which case the error will be dismissed when the user arrives at this activity.
     */
    public static Intent makeIntent(Context context, @StringRes int explanation, boolean isError) {
        return new Intent(context, DeviceUnsupportedActivity.class)
            .putExtra(EXTRA_EXPLANATION, explanation)
            .putExtra(EXTRA_IS_ERROR, isError);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.device_problem);
        setContentView(R.layout.activity_device_unsupported);

        MaterialToolbar appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);

        @StringRes int explanationStringResource = getIntent().getIntExtra(EXTRA_EXPLANATION, 0);
        if (explanationStringResource == 0)
            throw new RuntimeException("Explanation not provided");
        
        TextView explanationView = findViewById(R.id.device_unsupported_explanation);
        explanationView.setText(explanationStringResource);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        boolean isError = getIntent().getBooleanExtra(EXTRA_IS_ERROR, false);
        if (isError)
            new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(this);
    }
    
}
