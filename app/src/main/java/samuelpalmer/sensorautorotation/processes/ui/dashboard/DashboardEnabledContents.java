package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceClient;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;


public class DashboardEnabledContents extends Fragment {
	
	private ImageView userIcon;
	private View sensorProgress;
	private TextView statusText;
	private ImageButton changeSideButton;
	private ImageButton turnOffButton;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.dashboard_enabled_contents, container, false);
		
		userIcon = layout.findViewById(R.id.dashboard_user_icon);
		sensorProgress = layout.findViewById(R.id.dashboard_sensor_progress);
		
		statusText = layout.findViewById(R.id.dashboard_status);
		changeSideButton = layout.findViewById(R.id.dashboard_button_change_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		
		userIcon.setColorFilter(ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500));
		
		changeSideButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ApplicationInstanceClient.send(new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_CHANGE_SIDE));
				//Not showing a progress bar here since most of the time the update will be instant, in which case flicker would occur
			}
			
		});
		
		turnOffButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ServiceSynchroniser.disableService();
				getActivity().finish();
			}
			
		});
		
		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		changeSideButton.setImageResource(R.drawable.dashboard_change_side);
		turnOffButton.setImageResource(R.drawable.dashboard_turn_off);
		
		return layout;
	}
	
	public void setStatus(ApplicationStatus lastStatus) {
		if (lastStatus.waitingForSensor) {
			ViewHelpers.ensureVisibility(userIcon, View.GONE);
			ViewHelpers.ensureVisibility(sensorProgress, View.VISIBLE);
		}
		else {
			userIcon.setImageResource(lastStatus.user.iconId());
			userIcon.setContentDescription(CurrentProcess.context().getText(lastStatus.user.descriptionId()));
			ViewHelpers.ensureVisibility(userIcon, View.VISIBLE);
			ViewHelpers.ensureVisibility(sensorProgress, View.GONE);
		}
		
		statusText.setText(lastStatus.waitingForSensor ? R.string.status_waiting_sensor : lastStatus.user.descriptionId());
	}
}
