package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

/**
 * The main content of the dashboard. This fragment requires the application status to be available.
 */
@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class DashboardNormalFragment extends Fragment {

    private StatusChangeListener parentStatusListener;
    private CommandSentListener commandSentListener;

    private CoordinatorLayout snackbarContainer;
    private ProgressBar progressBar;
    private ViewGroup statusContainer;
    private Snackbar snackbar;
    
    private View waitingForSensor;
    private View leftSideButton;
    private View turnOffButton;
    private View rightSideButton;

    private ApplicationStatusClient statusClient;
    private ApplicationStatus lastStatus;

    interface StatusChangeListener {
        void onStatusChange(ApplicationStatus status);
    }

    interface CommandSentListener {
        void onCommandSent(boolean isStartCommand);
    }

    public View getRightSideButton() {
        if (rightSideButton == null)
            throw new IllegalStateException("Button is not yet available");

        return rightSideButton;
    }

    public View getOffButton() {
        if (turnOffButton == null)
            throw new IllegalStateException("Button is not yet available");

        return turnOffButton;
    }

    public ApplicationStatus getStatus() {
        return lastStatus;
    }

    public void setStatusChangeListener(StatusChangeListener statusChangeListener) {
        parentStatusListener = statusChangeListener;
    }

    public void setCommandSentListener(CommandSentListener commandSentListener) {
        this.commandSentListener = commandSentListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_dashboard_normal, container, false);
        
        snackbarContainer = layout.findViewById(R.id.dashboard_error_container);
        progressBar = layout.findViewById(R.id.dashboard_progress);
        statusContainer = layout.findViewById(R.id.dashboard_status);
        
        waitingForSensor = layout.findViewById(R.id.dashboard_normal_waiting_sensor);
        
        leftSideButton = layout.findViewById(R.id.dashboard_button_left_side);
        turnOffButton = layout.findViewById(R.id.dashboard_button_turn_off);
        rightSideButton = layout.findViewById(R.id.dashboard_button_right_side);
        
        leftSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_LEFT_SIDE, true));
        turnOffButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_TURN_OFF, false));
        rightSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_RIGHT_SIDE, true));
        
        return layout;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        statusClient = new ApplicationStatusClient(getContext(), statusChanged);
    }

    private class ActionClickListener implements View.OnClickListener {
        
        private final String action;
        private final boolean isStartCommand;

        private ActionClickListener(String action, boolean isStartCommand) {
            this.action = action;
            this.isStartCommand = isStartCommand;
        }
        
        @Override
        public void onClick(View view) {
            if (!view.isSelected()) {
                // Including config in the command to ensure that the app is started with the latest copy of the user preferences, just in case it got out of sync.
                new ApplicationCommand(
                    action,
                    ApplicationSettings.getSharedPreferences(getContext())
                ).send(getContext());

                if (commandSentListener != null)
                    commandSentListener.onCommandSent(isStartCommand);
            }
        }
        
    }

    @Override
    public void onStart() {
        super.onStart();
        statusClient.subscribe();
        showStatus();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        statusClient.unsubscribe();
    }
    
    private final ApplicationStatusClient.Listener statusChanged = new ApplicationStatusClient.Listener() {
        
        @Override
        public void onChange(final ApplicationStatus status) {
            if (status != null) {
                lastStatus = status;
                showStatus();

                if (parentStatusListener != null)
                    parentStatusListener.onStatusChange(status);
            }
        }
        
    };
    
    private void showStatus() {
        // Intentionally preserving space so the dashboard doesn't resize once it finished loading.
        ViewUtilities.ensureVisibility(progressBar, lastStatus == null ? View.VISIBLE : View.INVISIBLE);
        ViewUtilities.ensureVisibility(statusContainer, lastStatus == null ? View.INVISIBLE : View.VISIBLE);
        
        if (lastStatus != null) {
            waitingForSensor.setVisibility(lastStatus.runningState != null && lastStatus.runningState.waitingForSensor ? View.VISIBLE : View.GONE);
            
            leftSideButton.setSelected(false);
            turnOffButton.setSelected(false);
            rightSideButton.setSelected(false);
            
            if (lastStatus.runningState == null)
                turnOffButton.setSelected(true);
            else if (lastStatus.runningState.userSide.equals(UserSide.LEFT))
                leftSideButton.setSelected(true);
            else
                rightSideButton.setSelected(true);
            
            updateSnackbar();
        }
    }

    private void updateSnackbar() {
        if (snackbar == null) {
            snackbar = Snackbar.make(snackbarContainer, "", Snackbar.LENGTH_INDEFINITE);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    
                    if (event == DISMISS_EVENT_SWIPE)
                        new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(getContext());
                    
                    //If the user swipes away the snackbar, it can't be shown anymore, so this one is effectively dead
                    DashboardNormalFragment.this.snackbar = null;
                }
            });
        }
        
        if (lastStatus.error == null)
            snackbar.dismiss();
        else {
            snackbar.setText(lastStatus.error.getSummary());
            snackbar.setAction(R.string.environment_error_more, v -> {
                try {
                    lastStatus.error.showError(getContext()).send();
                } catch (PendingIntent.CanceledException e) {
                    throw new RuntimeException(e);
                }
            });
            snackbar.show();
        }
    }
    
}
