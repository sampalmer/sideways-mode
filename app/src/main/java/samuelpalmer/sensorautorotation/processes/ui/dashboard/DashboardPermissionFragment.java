package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.AppOpMonitor;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

public class DashboardPermissionFragment extends Fragment {

	private AppOpMonitor permissionMonitor;

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
			startActivity(intent);

			permissionMonitor = new AppOpMonitor(context, AppOpsManager.OPSTR_WRITE_SETTINGS);
			permissionMonitor.subscribe(permissionChanged);
		}
		catch (ActivityNotFoundException e) {
			throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (permissionMonitor != null) {
			permissionMonitor.unsubscribe(permissionChanged);
			permissionMonitor = null;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private final EventHandler permissionChanged = new EventHandler() {
		@Override
		public void update() {
			if (permissionMonitor.hasPermission()) {
				FragmentActivity activity = getActivity();
				if (activity != null)
					ActivityUtils.finishActivitiesOnTopOf(activity);

				permissionMonitor.unsubscribe(permissionChanged);
				permissionMonitor = null;
			}
		}
	};

}
