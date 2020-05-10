package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

public abstract class DialogActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();

        // Prevents the error: "Activity {samuelpalmer.sensorautorotation/samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity} did not call finish() prior to onResume() completing"
        if (Build.VERSION.SDK_INT >= 23)
            setVisible(true);
    }

}
