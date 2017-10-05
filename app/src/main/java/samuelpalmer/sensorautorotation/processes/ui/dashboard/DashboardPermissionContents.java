package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.SystemIntents;

public class DashboardPermissionContents extends Fragment {
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.dashboard_permission_contents, container, false);
		
		Button fixButton = layout.findViewById(R.id.dashboard_permission_fix);
		
		fixButton.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onClick(View view) {
				try {
					startActivity(new SystemIntents(getContext()).modifySystemSettings());
				}
				catch (ActivityNotFoundException e) {
					throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
				}
			}
		});
		
		return layout;
	}
	
}
