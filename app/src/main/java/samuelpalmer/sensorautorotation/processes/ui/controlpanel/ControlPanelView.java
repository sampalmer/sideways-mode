package samuelpalmer.sensorautorotation.processes.ui.controlpanel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceCommand;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ControlPanelView extends RelativeLayout {

	private final ServiceStatusContentProvider.Client serviceClient;
	private final Handler handler;
	private final Settings.Setting<Boolean> enabledSetting;

	private TextView disabledText;
	private ProgressBar progressBar;
	private ViewGroup contents;
	private ImageView userIcon;
	private TextView statusText;
	private ImageButton toggleModeButton;

	private TextView modeText;
	private boolean childrenInflated;
	private ServiceStatus lastStatus;
	private boolean isEnabled;

	public ControlPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		serviceClient = new ServiceStatusContentProvider.Client(getContext());
		handler = new Handler();
		enabledSetting = ApplicationSettings.remote().enabled();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		ImageButton settingsButton = (ImageButton) findViewById(R.id.control_panel_settings);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getContext().startActivity(new Intent(getContext(), MainActivity.class));
				ControlPanelCoordinator.dismiss();
			}
		});

		disabledText = (TextView) findViewById(R.id.control_panel_disabled);
		progressBar = (ProgressBar) findViewById(R.id.control_panel_progress);
		contents = (ViewGroup) findViewById(R.id.control_panel_contents);

		userIcon = (ImageView) findViewById(R.id.control_panel_user_icon);
		statusText = (TextView) findViewById(R.id.control_panel_status);
		ImageButton rotateLeftButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_left);
		toggleModeButton = (ImageButton) findViewById(R.id.control_panel_button_toggle);
		ImageButton rotateRightButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_right);
		modeText = (TextView) findViewById(R.id.control_panel_mode);

		userIcon.setColorFilter(ColourGetterCompat.getColor(CurrentProcess.context(), R.color.primary_500));

		rotateLeftButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_LEFT)));

		//Not showing the toast when this is pressed since it causes a flickering issue on Samsung Galaxy S5 (Android 6)
		toggleModeButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_TOGGLE)));

		rotateRightButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_RIGHT)));

		childrenInflated = true;
		enabledChanged.update();
		maybeUpdateUi();
	}

	private class RotationServiceClickListener implements View.OnClickListener {

		private final RotationServiceCommand command;

		public RotationServiceClickListener(RotationServiceCommand command) {
			this.command = command;
		}

		@Override
		public void onClick(View v) {
			RotationServiceClient.send(command);
			//Not showing a progress bar here since most of the time the update will be instant, in which case flicker would occur
		}

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		serviceClient.subscribe(statusChanged);
		enabledSetting.subscribe(enabledChanged);
		EnvironmentError.onChange.subscribe(lastErrorChanged);

		enabledChanged.update();
		maybeUpdateUi();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		serviceClient.unsubscribe(statusChanged);
		enabledSetting.unsubscribe(enabledChanged);
		EnvironmentError.onChange.unsubscribe(lastErrorChanged);
	}

	private final IServiceStatusListener.Stub statusChanged = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					lastStatus = status;
					maybeUpdateUi();
				}

			});
		}

	};

	private EventHandler enabledChanged = new EventHandler() {
		@Override
		public void update() {
			isEnabled = enabledSetting.get();
			maybeUpdateUi();
		}
	};

	private EventHandler lastErrorChanged = new EventHandler() {
		@Override
		public void update() {
			maybeUpdateUi();
		}
	};

	private void maybeUpdateUi() {
		if (childrenInflated)
			showStatus();
	}

	@SuppressLint("SetTextI18n")
	private void showStatus() {
		boolean isLoading = isEnabled && lastStatus == null;

		ensureVisibility(disabledText, isEnabled ? View.GONE : View.VISIBLE);
		ensureVisibility(progressBar, isLoading ? View.VISIBLE : View.GONE);

		if (isEnabled)
			//Don't want the view the view to change size after loading; it should stay the same
			ensureVisibility(contents, isLoading ? View.INVISIBLE : View.VISIBLE);
		else
			ensureVisibility(contents, View.GONE);

		if (!isEnabled)
			disabledText.setText(RotationServiceClient.disabledMessage());
		else {
			if (lastStatus != null) {
				userIcon.setImageResource(lastStatus.user.iconId());
				userIcon.setContentDescription(StringResource.get(lastStatus.user.descriptionId()));
				statusText.setText(" " + StringResource.get(lastStatus.user.longDescriptionId()));
				toggleModeButton.setImageResource(lastStatus.mode.controlPanelIcon());
				toggleModeButton.setContentDescription(StringResource.get(lastStatus.mode.toggleToOtherModeResourceId()));
				modeText.setText(lastStatus.mode.autoRotationStateResourceId());
			}
		}
	}

	/**
	 * Only changes the visibility if it's different to the current value.
	 * This is to minimise lag.
	 */
	private static void ensureVisibility(View control, int newVisibility) {
		int currentVisibility = control.getVisibility();
		if (currentVisibility != newVisibility)
			control.setVisibility(newVisibility);
	}

}
