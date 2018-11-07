package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;

public class DashboardPermissionFragment extends Fragment {
	
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
		Intent intent = SystemIntents.modifySystemSettings(getContext());
		
		try {
			startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
		}
	}
	
}
