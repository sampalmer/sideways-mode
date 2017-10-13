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


public class DashboardNormalFragment extends Fragment {
	
	private View sensorProgress;
	private TextView statusText;
	
	private ImageButton leftSideButton;
	private ImageButton turnOffButton;
	private ImageButton rightSideButton;
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);
		
		sensorProgress = layout.findViewById(R.id.dashboard_sensor_progress);
		
		statusText = layout.findViewById(R.id.dashboard_status);
		leftSideButton = layout.findViewById(R.id.dashboard_button_left_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		rightSideButton = layout.findViewById(R.id.dashboard_button_right_side);
		
		leftSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_LEFT_SIDE));
		turnOffButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_TURN_OFF));
		rightSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_RIGHT_SIDE));
		
		return layout;
	}
	
	private class ActionClickListener implements View.OnClickListener {
		
		private final String action;
		
		public ActionClickListener(String action) {
			this.action = action;
		}
		
		@Override
		public void onClick(View view) {
			if (!view.isSelected()) {
				new ApplicationCommand(action).send(getContext());
				getActivity().finish();
			}
		}
		
	}
	
	public void setStatus(ApplicationStatus lastStatus) {
		ViewHelpers.ensureVisibility(sensorProgress, lastStatus.waitingForSensor ? View.VISIBLE : View.GONE);
		statusText.setText(lastStatus.waitingForSensor ? R.string.status_waiting_sensor : lastStatus.appEnabled ? lastStatus.user.descriptionId : R.string.choose_side);
		
		leftSideButton.setSelected(false);
		turnOffButton.setSelected(false);
		rightSideButton.setSelected(false);
		
		if (!lastStatus.appEnabled)
			turnOffButton.setSelected(true);
		else if (lastStatus.user.equals(UserSide.LEFT))
			leftSideButton.setSelected(true);
		else
			rightSideButton.setSelected(true);
	}
}
