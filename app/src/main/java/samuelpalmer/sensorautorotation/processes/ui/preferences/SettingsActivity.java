package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.TransitionManager;

import com.google.android.material.appbar.MaterialToolbar;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class SettingsActivity extends UiActivity {

    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar appBar = findViewById(R.id.toolbar);
        setSupportActionBar(appBar);

        // Hook up the app bar with the navigation
        // Not using Navigation.findNavController() due to https://issuetracker.google.com/issues/142847973
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController);

        // This animates changes to the toolbar.
        // It's better to do this *after* connecting the toolbar to AndroidX navigation since the transition is shorter this way.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> TransitionManager.beginDelayedTransition(appBar));
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
