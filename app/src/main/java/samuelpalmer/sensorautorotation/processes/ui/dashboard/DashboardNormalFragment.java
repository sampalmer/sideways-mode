package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

/**
 * The main content of the dashboard. This fragment requires the application status to be available.
 */
public class DashboardNormalFragment extends Fragment {
	
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
		
		leftSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_LEFT_SIDE));
		turnOffButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_TURN_OFF));
		rightSideButton.setOnClickListener(new ActionClickListener(ApplicationCommand.ACTION_RIGHT_SIDE));
		
		return layout;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		statusClient = new ApplicationStatusClient(getContext(), statusChanged);
	}
	
	private class ActionClickListener implements View.OnClickListener {
		
		private final String action;
		
		public ActionClickListener(String action) {
			this.action = action;
		}
		
		@Override
		public void onClick(View view) {
			if (!view.isSelected()) {
				// Including config in the command to ensure that the app is started with the latest copy of the user preferences, just in case it got out of sync.
				new ApplicationCommand(action, SendConfigReceiver.getConfigToSync(getContext())).send(getContext());
				
				//noinspection ConstantConditions
				getActivity().finish();
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

			// TODO: Add a pause before launching the tutorial
			// TODO: Make the entire UI non-interactive during the tutorial
			// TODO: When locating the menu items, only match against *visible* views
			// TODO: In each callback, check that everything is still in a valid state before proceeding

			DashboardActivity activity = (DashboardActivity) getActivity();
			TapTarget leftButtonTarget = TapTarget.forView(leftSideButton, "Getting started", "When you lie you on your side, tell Sideways Mode which side you're on");
			TapTargetView.showFor(activity.dialog, leftButtonTarget, new TapTargetView.Listener() {
				@Override
				public void onTargetCancel(TapTargetView view) {
					super.onTargetCancel(view);
					proceed();
				}

				@Override
				public void onTargetClick(TapTargetView view) {
					super.onTargetClick(view);
					proceed();
				}

				private void proceed() {
					new Handler().postDelayed(() -> {
						activity.popup.show();

						// Wait for the popup menu to appear
						new Handler().postDelayed(() -> {
							View incompatibleAppsMenuItemText = getMenuItemText(activity, R.id.action_incompatible_apps);
							View uninstallMenuItemText = getMenuItemText(activity, R.id.action_uninstall);

							ViewGroup.LayoutParams layoutParams;

							layoutParams = incompatibleAppsMenuItemText.getLayoutParams();
							layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
							incompatibleAppsMenuItemText.setLayoutParams(layoutParams);

							layoutParams = uninstallMenuItemText.getLayoutParams();
							layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
							uninstallMenuItemText.setLayoutParams(layoutParams);

							TapTarget incompatibleAppsTarget = TapTarget.forView(incompatibleAppsMenuItemText, "Some apps don't work with Sideways Mode", "If an app doesn't rotate correctly, add it here to fix it")
									.targetRadius(70);
							TapTarget uninstallTarget = TapTarget.forView(uninstallMenuItemText, "Don't want Sideways Mode?", "If you decide to uninstall Sideways Mode, uninstall it here so the screen doesn't get stuck");

							new TapTargetSequence(activity.dialog)
									.continueOnCancel(true)
									.listener(new TapTargetSequence.Listener() {
										@Override
										public void onSequenceFinish() {
											new Handler().postDelayed(() -> {
												activity.popup.dismiss();
											}, 500);
										}

										@Override
										public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {}

										@Override
										public void onSequenceCanceled(TapTarget lastTarget) {}
									})
									.targets(incompatibleAppsTarget, uninstallTarget)
									.start();
						}, 500);
					}, 500);
				}
			});
		}
	}

	private View getMenuItemText(DashboardActivity activity, int menuItem) {
		View targetMenuItemText = null;

		List<View> windowManagerViews = getWindowManagerViews();
		for (View root : windowManagerViews) {
			if (targetMenuItemText == null)
				targetMenuItemText = findViewByText(root, activity.popup.getMenu().findItem(menuItem).getTitle());
		}
		return targetMenuItemText;
	}

	public static List<View> getWindowManagerViews() {
		try {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
					Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {

				// get the list from WindowManagerImpl.mViews
				Class wmiClass = Class.forName("android.view.WindowManagerImpl");
				Object wmiInstance = wmiClass.getMethod("getDefault").invoke(null);

				return viewsFromWM(wmiClass, wmiInstance);

			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

				// get the list from WindowManagerGlobal.mViews
				Class wmgClass = Class.forName("android.view.WindowManagerGlobal");
				Object wmgInstance = wmgClass.getMethod("getInstance").invoke(null);

				return viewsFromWM(wmgClass, wmgInstance);
			}

		} catch (Exception e) {
			e.printStackTrace(); // TODO: Log this properly
		}

		return new ArrayList<View>();
	}

	private static List<View> viewsFromWM(Class wmClass, Object wmInstance) throws Exception {
		Field viewsField = wmClass.getDeclaredField("mViews");
		viewsField.setAccessible(true);
		Object views = viewsField.get(wmInstance);

		if (views instanceof List) {
			return (List<View>) viewsField.get(wmInstance);
		} else if (views instanceof View[]) {
			return Arrays.asList((View[])viewsField.get(wmInstance));
		}

		return new ArrayList<View>();
	}


	private View findViewByText(View view, CharSequence targetText) {
		Log.d(DashboardNormalFragment.class.getSimpleName(), view.getClass().getSimpleName());

		if (view instanceof TextView)
		{
			TextView textView = (TextView) view;
			CharSequence text = textView.getText();
			Log.d(DashboardNormalFragment.class.getSimpleName(), "TEXT: " + text);

			if (text.equals(targetText))
				return view;
		}
		else if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;

			for (int i = 0; i < viewGroup.getChildCount(); ++i) {
				View childView = viewGroup.getChildAt(i);
				View result = findViewByText(childView, targetText);
				if (result != null)
					return result;
			}
		}

		return null;
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
