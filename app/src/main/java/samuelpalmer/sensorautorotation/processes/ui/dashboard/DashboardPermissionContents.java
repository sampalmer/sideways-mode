package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.SystemIntents;

public class DashboardPermissionContents extends LinearLayout {
	
	private Button fixButton;
	
	public DashboardPermissionContents(Context context) {
		super(context);
		init();
	}
	
	public DashboardPermissionContents(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DashboardPermissionContents(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public DashboardPermissionContents(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.dashboard_permission_contents, this, true);
		
		fixButton = findViewById(R.id.dashboard_permission_fix);
		
		fixButton.setOnClickListener(new View.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onClick(View view) {
				try {
					getContext().startActivity(new SystemIntents(getContext()).modifySystemSettings());
				}
				catch (ActivityNotFoundException e) {
					throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
				}
				
				// If we're in a quick setting dialog, the dialog can end up over the top of the system settings activity, so we need to close the dialog.
				if (DashboardDialogCoordinator.dialogExists())
					DashboardDialogCoordinator.dismissDialog();
			}
		});
	}
	
}
