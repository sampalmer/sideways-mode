package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;


public class DashboardEnabledFragment extends Fragment {
	
	private ImageView userIcon;
	private View sensorProgress;
	private TextView statusText;
	private ImageButton changeSideButton;
	private ImageButton turnOffButton;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_enabled, container, false);
		
		userIcon = layout.findViewById(R.id.dashboard_user_icon);
		sensorProgress = layout.findViewById(R.id.dashboard_sensor_progress);
		
		statusText = layout.findViewById(R.id.dashboard_status);
		changeSideButton = layout.findViewById(R.id.dashboard_button_change_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		
		changeSideButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ApplicationCommand(ApplicationCommand.ACTION_CHANGE_SIDE).send(getContext());
				//Not showing a progress bar here since most of the time the update will be instant, in which case flicker would occur
			}
			
		});
		
		turnOffButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(getContext());
				getActivity().finish();
			}
			
		});
		
		return layout;
	}
	
	public void setStatus(ApplicationStatus lastStatus) {
		if (lastStatus.waitingForSensor) {
			ViewHelpers.ensureVisibility(userIcon, View.GONE);
			ViewHelpers.ensureVisibility(sensorProgress, View.VISIBLE);
		}
		else {
			userIcon.setImageResource(lastStatus.user.iconId);
			userIcon.setContentDescription(CurrentProcess.context().getText(lastStatus.user.descriptionId));
			ViewHelpers.ensureVisibility(userIcon, View.VISIBLE);
			ViewHelpers.ensureVisibility(sensorProgress, View.GONE);
		}
		
		statusText.setText(lastStatus.waitingForSensor ? R.string.status_waiting_sensor : lastStatus.user.descriptionId);
	}
}
