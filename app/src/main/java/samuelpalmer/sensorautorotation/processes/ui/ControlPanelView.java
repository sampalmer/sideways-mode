package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ControlPanelView extends LinearLayout {

	private final ServiceStatusContentProvider.Client serviceClient;
	private final Handler handler;
	private final Settings.Setting<Boolean> enabledSetting;

	private ViewGroup controls;
	private ImageButton toggleModeButton;

	private TextView statusText;
	private boolean childrenInflated;
	private ServiceStatus lastStatus;

	public ControlPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		serviceClient = new ServiceStatusContentProvider.Client(getContext());
		handler = new Handler();
		enabledSetting = ApplicationSettings.remote().enabled();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		controls = (ViewGroup) findViewById(R.id.control_panel_controls);

		statusText = (TextView) findViewById(R.id.control_panel_status);
		ImageButton rotateLeftButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_left);
		toggleModeButton = (ImageButton) findViewById(R.id.control_panel_button_toggle);
		ImageButton rotateRightButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_right);

		rotateLeftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RotationServiceClient.send(RotationService.ACTION_ROTATE_LEFT);
			}
		});

		toggleModeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RotationServiceClient.send(RotationService.ACTION_TOGGLE);
			}
		});

		rotateRightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RotationServiceClient.send(RotationService.ACTION_ROTATE_RIGHT);
			}
		});

		for (ImageButton button : new ImageButton[] { rotateLeftButton, toggleModeButton, rotateRightButton })
			button.setColorFilter(ColourGetterCompat.getColor(CurrentProcess.context(), R.color.primary_500));

		childrenInflated = true;
		enabledChanged.update();
		maybeShowStatus();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		serviceClient.subscribe(statusChanged);
		enabledSetting.subscribe(enabledChanged);

		enabledChanged.update();
		maybeShowStatus();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		serviceClient.unsubscribe(statusChanged);
		enabledSetting.unsubscribe(enabledChanged);
	}

	private final IServiceStatusListener.Stub statusChanged = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					lastStatus = status;
					maybeShowStatus();
				}

			});
		}

	};

	private EventHandler enabledChanged = new EventHandler() {
		@Override
		public void update() {
			if (childrenInflated) {
				boolean isEnabled = enabledSetting.get();
				controls.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
				if (!isEnabled)
					statusText.setText(RotationServiceClient.disabledMessage());
			}
		}
	};

	private void maybeShowStatus() {
		if (childrenInflated && lastStatus != null)
			showStatus(lastStatus);
	}

	private void showStatus(ServiceStatus status) {
		statusText.setText(status.getSummary());
		toggleModeButton.setImageResource(status.mode.actionIcon());
		toggleModeButton.setContentDescription(StringResource.get(status.mode.toggleToOtherModeResourceId()));
	}

}
