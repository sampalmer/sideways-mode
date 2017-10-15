package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.ui.SystemIntents;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;


public class DashboardNormalFragment extends Fragment {
	
	private static final int REQUEST_CODE_BUTTON_PRESSED = 1;
	private static final String KEY_PENDING_ACTION = "pending_action";
	
	private View sensorProgress;
//	private TextView statusText;
	
	private View leftSideButton;
	private View turnOffButton;
	private View rightSideButton;
	
	private String pendingAction;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_PENDING_ACTION, pendingAction);
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);
		
//		sensorProgress = layout.findViewById(R.id.dashboard_sensor_progress);
		
//		statusText = layout.findViewById(R.id.dashboard_status);
		leftSideButton = layout.findViewById(R.id.dashboard_button_left_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		rightSideButton = layout.findViewById(R.id.dashboard_button_right_side);
		
		leftSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_LEFT_SIDE));
		turnOffButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_TURN_OFF));
		rightSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_RIGHT_SIDE));
		
		if (savedInstanceState != null)
			pendingAction = savedInstanceState.getString(KEY_PENDING_ACTION);
		
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
				if (!SystemSettings.canWrite(DashboardNormalFragment.this.getContext())) {
					pendingAction = action;
					Intent intent = new SystemIntents(getContext()).modifySystemSettings();
					
					try {
						startActivityForResult(intent, REQUEST_CODE_BUTTON_PRESSED);
					}
					catch (ActivityNotFoundException e) {
						throw new RuntimeException("Couldn't find 'modify system settings' activity. This should have been caught by the startup check.", e);
					}
				} else
					buttonPressed(action);
			}
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_BUTTON_PRESSED) {
			if (SystemSettings.canWrite(getContext()))
				buttonPressed(pendingAction);
			else
				pendingAction = null; // The user might have returned without actually granting the permission
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void buttonPressed(String action) {
		// Including config in the command to ensure that the app is started with the latest copy of the user preferences, just in case it got out of sync.
		new ApplicationCommand(action, SendConfigReceiver.getConfigToSync()).send(getContext());
		getActivity().finish();
		pendingAction = null;
	}
	
	public void setStatus(ApplicationStatus lastStatus) {
//		ViewHelpers.ensureVisibility(sensorProgress, lastStatus.waitingForSensor ? View.VISIBLE : View.GONE);
//		statusText.setText(lastStatus.waitingForSensor ? R.string.status_waiting_sensor : lastStatus.appEnabled ? lastStatus.user.descriptionId : R.string.choose_side);
		
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
