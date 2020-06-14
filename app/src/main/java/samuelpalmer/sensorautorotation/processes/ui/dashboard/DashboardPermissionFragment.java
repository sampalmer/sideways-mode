package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;

@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class DashboardPermissionFragment extends Fragment {

    private static final int REQUEST_CODE_GRANT_PERMISSION = 1;

    private Observer<Boolean> permissionObserver;
    private DashboardDialogViewModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_dashboard_permission, container, false);
        
        TextView description = layout.findViewById(R.id.dashboard_permission_description);
        description.setText(getString(R.string.permission_needed_system_settings, getText(R.string.app_name_full)));
        
        Button fixButton = layout.findViewById(R.id.dashboard_permission_fix);
        fixButton.setOnClickListener(this::grantPermission);
        
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = new ViewModelProvider(getActivity()).get(DashboardDialogViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void grantPermission(@SuppressWarnings("unused") View view) {
        Context context = getContext();
        assert context != null; // Because the user has just clicked a button, so the activity must be running

        Intent intent = SystemIntents.modifySystemSettings(context);

        DashboardActivity dashboardActivity = (DashboardActivity) requireActivity();
        dashboardActivity.unlockAndRun(() -> {
            try {
                startActivityForResult(intent, REQUEST_CODE_GRANT_PERMISSION);

                if (permissionObserver == null) {
                    permissionObserver = DashboardPermissionFragment.this::checkPermissionGranted;

                    // Not tying the observer to our lifecycle since we need to continue observing
                    // while we're behind the system permission activity
                    model.getPermissionToStart().observeForever(permissionObserver);
                }
            }
            catch (ActivityNotFoundException e) {
                throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribePermissionMonitor();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissionGranted(boolean hasPermission) {
        if (hasPermission) {
            FragmentActivity activity = getActivity();
            if (activity != null)
                ActivityUtils.finishActivitiesOnTopOf(activity);

            unsubscribePermissionMonitor();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void unsubscribePermissionMonitor() {
        if (permissionObserver != null) {
            model.getPermissionToStart().removeObserver(permissionObserver);
            permissionObserver = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GRANT_PERMISSION)
            unsubscribePermissionMonitor();
        else
            Log.w(DashboardPermissionFragment.class.getSimpleName(), "Got back unknown request code: " + requestCode);
    }
}
