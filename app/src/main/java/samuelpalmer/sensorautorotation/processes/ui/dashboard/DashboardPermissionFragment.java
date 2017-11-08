package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;

public class DashboardPermissionFragment extends Fragment {
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_permission, container, false);
		
		Button fixButton = layout.findViewById(R.id.dashboard_permission_fix);
		
		fixButton.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onClick(View view) {
				Intent intent = SystemIntents.modifySystemSettings(getContext());
				
				try {
					startActivity(intent);
				}
				catch (ActivityNotFoundException e) {
					throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
				}
			}
		});
		
		return layout;
	}
	
}
