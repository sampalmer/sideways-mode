package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import samuelpalmer.sensorautorotation.processes.ui.onboarding.OnboardingActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public abstract class UiActivity extends AppCompatActivity {
    
    @Override
    protected void onStart() {
        super.onStart();
        
        //Checking this after in onStart() instead of onCreate() in case the user wants to go out and back in again to retry the check in case the problem is intermittent
        int startupErrorString = AppCompatibilityCheck.check(this);
        if (startupErrorString != 0) {
            //Not letting the user go any further since they shouldn't have installed the app
            Log.w(getClass().getSimpleName(), getText(startupErrorString).toString());
            Intent intent = new Intent(this, DeviceUnsupportedActivity.class)
                .putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, startupErrorString)
                .putExtra(DeviceUnsupportedActivity.EXTRA_IS_ERROR, false)
            ;
            startActivity(intent);
            
            finish();
            return;
        }

        if (!(this instanceof OnboardingActivity)) {
            SharedPreferences applicationSettings = ApplicationSettings.getSharedPreferences(this);
            boolean onboarded = applicationSettings.getBoolean(ApplicationSettings.KEY_USER_ONBOARDED, false);

            if (!onboarded) {
                Intent intent = new Intent(this, OnboardingActivity.class);
                startActivity(intent);
            }
        }
    }
    
}
