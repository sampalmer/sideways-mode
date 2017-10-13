package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;


public class DashboardEnabledFragment extends Fragment {
	
	private View sensorProgress;
	private TextView statusText;
	
	private ImageButton leftSideButton;
	private ImageButton turnOffButton;
	private ImageButton rightSideButton;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_enabled, container, false);
		
		sensorProgress = layout.findViewById(R.id.dashboard_sensor_progress);
		
		statusText = layout.findViewById(R.id.dashboard_status);
		leftSideButton = layout.findViewById(R.id.dashboard_button_left_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		rightSideButton = layout.findViewById(R.id.dashboard_button_right_side);
		
		leftSideButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ApplicationCommand(ApplicationCommand.ACTION_LEFT_SIDE).send(getContext());
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
		
		rightSideButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ApplicationCommand(ApplicationCommand.ACTION_RIGHT_SIDE).send(getContext());
				//Not showing a progress bar here since most of the time the update will be instant, in which case flicker would occur
			}
			
		});
		
		return layout;
	}
	
	public void setStatus(ApplicationStatus lastStatus) {
		if (lastStatus.waitingForSensor) {
			ViewHelpers.ensureVisibility(sensorProgress, View.VISIBLE);
		}
		else {
			ViewHelpers.ensureVisibility(sensorProgress, View.GONE);
			
			if (lastStatus.user.equals(UserSide.LEFT)) {
				leftSideButton.setSelected(true);
				rightSideButton.setSelected(false);
			}
			else {
				leftSideButton.setSelected(false);
				rightSideButton.setSelected(true);
			}
		}
		
		statusText.setText(lastStatus.waitingForSensor ? R.string.status_waiting_sensor : lastStatus.user.descriptionId);
	}
}
