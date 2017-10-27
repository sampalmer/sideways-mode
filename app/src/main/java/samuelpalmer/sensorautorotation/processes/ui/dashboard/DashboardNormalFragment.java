package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

/**
 * The main content of the dashboard. This fragment requires the application status to be available.
 */
public class DashboardNormalFragment extends Fragment {
	
	private static final int REQUEST_CODE_BUTTON_PRESSED = 1;
	private static final String KEY_PENDING_ACTION = "pending_action";
	private static final String KEY_ACTION_IN_PROGRESS = "action_in_progress";
	
	private View waitingForSensor;
	private View leftSideButton;
	private View turnOffButton;
	private View rightSideButton;
	
	// The button the user pressed while the app didn't have access to write system settings.
	private String pendingButtonPress;
	private String actionInProgress;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_PENDING_ACTION, pendingButtonPress);
		outState.putString(KEY_ACTION_IN_PROGRESS, actionInProgress);
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);
		
		waitingForSensor = layout.findViewById(R.id.dashboard_normal_waiting_sensor);
		leftSideButton = layout.findViewById(R.id.dashboard_button_left_side);
		turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
		rightSideButton = layout.findViewById(R.id.dashboard_button_right_side);
		
		leftSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_LEFT_SIDE));
		turnOffButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_TURN_OFF));
		rightSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_RIGHT_SIDE));
		
		if (savedInstanceState != null) {
			pendingButtonPress = savedInstanceState.getString(KEY_PENDING_ACTION);
			actionInProgress = savedInstanceState.getString(KEY_ACTION_IN_PROGRESS);
		}
		
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
					pendingButtonPress = action;
					Intent intent = SystemIntents.modifySystemSettings(getContext());
					
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
				buttonPressed(pendingButtonPress);
			else
				pendingButtonPress = null; // The user might have returned without actually granting the permission
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void buttonPressed(String action) {
		// Including config in the command to ensure that the app is started with the latest copy of the user preferences, just in case it got out of sync.
		new ApplicationCommand(action, SendConfigReceiver.getConfigToSync(getContext())).send(getContext());
		pendingButtonPress = null;
		actionInProgress = action;
	}
	
	public void setStatus(ApplicationStatus status) {
		if (actionInProgress != null && actionIsCompleted(actionInProgress, status)) {
			getActivity().finish();
			actionInProgress = null;
			return; // So the UI doesn't update and cause a flicker while the activity closes.
		}
		
		waitingForSensor.setVisibility(status.runningState != null && status.runningState.waitingForSensor ? View.VISIBLE : View.GONE);
		
		leftSideButton.setSelected(false);
		turnOffButton.setSelected(false);
		rightSideButton.setSelected(false);
		
		if (status.runningState == null)
			turnOffButton.setSelected(true);
		else if (status.runningState.userSide.equals(UserSide.LEFT))
			leftSideButton.setSelected(true);
		else
			rightSideButton.setSelected(true);
	}
	
	private static boolean actionIsCompleted(String action, ApplicationStatus status) {
		return
			action.equals(ApplicationCommand.ACTION_LEFT_SIDE) && status.runningState != null && status.runningState.userSide.equals(UserSide.LEFT) && !status.runningState.waitingForSensor
			||
			action.equals(ApplicationCommand.ACTION_TURN_OFF) && status.runningState == null
			||
			action.equals(ApplicationCommand.ACTION_RIGHT_SIDE) && status.runningState != null && status.runningState.userSide.equals(UserSide.RIGHT) && !status.runningState.waitingForSensor;
	}
}
