package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class SettingsActivity extends UiActivity {

    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Hook up the app bar with the navigation
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navController = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null)
            return navController.navigateUp();
        else
            return false;
    }

}
