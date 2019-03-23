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
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class DashboardPermissionFragment extends Fragment {

    private static int REQUEST_CODE_GRANT_PERMISSION = 1;

    private EventRegistrar permissionMonitor;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void grantPermission(@SuppressWarnings("unused") View view) {
        Context context = getContext();
        assert context != null; // Because the user has just clicked a button, so the activity must be running

        Intent intent = SystemIntents.modifySystemSettings(context);
        
        try {
            startActivityForResult(intent, REQUEST_CODE_GRANT_PERMISSION);

            if (permissionMonitor == null) {
                permissionMonitor = ScreenOrientationController.makePermissionMonitor(context);
                permissionMonitor.subscribe(permissionChanged);
            }
        }
        catch (ActivityNotFoundException e) {
            throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribePermissionMonitor();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private final EventHandler permissionChanged = () -> {
        if (ScreenOrientationController.hasPermissionToStart(getContext())) {
            FragmentActivity activity = getActivity();
            if (activity != null)
                ActivityUtils.finishActivitiesOnTopOf(activity);

            unsubscribePermissionMonitor();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void unsubscribePermissionMonitor() {
        if (permissionMonitor != null) {
            permissionMonitor.unsubscribe(permissionChanged);
            permissionMonitor = null;
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
