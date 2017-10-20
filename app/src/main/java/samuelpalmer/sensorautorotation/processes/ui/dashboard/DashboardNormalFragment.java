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
import samuelpalmer.sensorautorotation.ApplicationStatus;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.ui.SystemIntents;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;


public class DashboardNormalFragment extends Fragment {
	
	private static final int REQUEST_CODE_BUTTON_PRESSED = 1;
	private static final String KEY_PENDING_ACTION = "pending_action";
	
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
		
		if (savedInstanceState != null)
			pendingButtonPress = savedInstanceState.getString(KEY_PENDING_ACTION);
		
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
				buttonPressed(pendingButtonPress);
			else
				pendingButtonPress = null; // The user might have returned without actually granting the permission
		}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void buttonPressed(String action) {
		// Including config in the command to ensure that the app is started with the latest copy of the user preferences, just in case it got out of sync.
		new ApplicationCommand(action, SendConfigReceiver.getConfigToSync()).send(getContext());
		pendingButtonPress = null;
		actionInProgress = action;
	}
	
	public void setStatus(ApplicationStatus status) {
		if (actionInProgress != null && actionIsCompleted(actionInProgress, status)) {
				getActivity().finish();
				actionInProgress = null;
				return; // So the UI doesn't update and cause a flicker while the activity closes.
			}
		
		waitingForSensor.setVisibility(status.waitingForSensor ? View.VISIBLE : View.GONE);
		
		leftSideButton.setSelected(false);
		turnOffButton.setSelected(false);
		rightSideButton.setSelected(false);
		
		if (!status.appEnabled)
			turnOffButton.setSelected(true);
		else if (status.user.equals(UserSide.LEFT))
			leftSideButton.setSelected(true);
		else
			rightSideButton.setSelected(true);
	}
	
	private static boolean actionIsCompleted(String action, ApplicationStatus status) {
		return
			action.equals(ApplicationCommand.ACTION_LEFT_SIDE) && UserSide.LEFT.equals(status.user) && !status.waitingForSensor
			||
			action.equals(ApplicationCommand.ACTION_TURN_OFF) && !status.appEnabled
			||
			action.equals(ApplicationCommand.ACTION_RIGHT_SIDE) && UserSide.RIGHT.equals(status.user) && !status.waitingForSensor;
	}
}
