package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;

public class ControlPanelView extends LinearLayout {

	private ServiceStatusContentProvider.Client serviceClient;
	private Handler handler;
	private ServiceStatus lastStatus;
	private ImageButton toggleButton;
	private TextView statusText;
	private boolean childrenInflated;

	public ControlPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		serviceClient = new ServiceStatusContentProvider.Client(getContext());
		handler = new Handler();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		statusText = (TextView) findViewById(R.id.control_panel_status);
		ImageButton rotateLeftButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_left);
		toggleButton = (ImageButton) findViewById(R.id.control_panel_button_toggle);
		ImageButton rotateRightButton = (ImageButton) findViewById(R.id.control_panel_button_rotate_right);

		rotateLeftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RotationServiceClient.send(RotationService.ACTION_ROTATE_LEFT);
			}
		});

		toggleButton.setOnClickListener(new View.OnClickListener() {
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

		for (ImageButton button : new ImageButton[] { rotateLeftButton, toggleButton, rotateRightButton })
			button.setColorFilter(ColourGetterCompat.getColor(CurrentProcess.context(), R.color.primary_500));

		childrenInflated = true;
		maybeUpdate();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		serviceClient.subscribe(listener);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		serviceClient.unsubscribe(listener);
	}

	private final IServiceStatusListener.Stub listener = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					lastStatus = status;
					maybeUpdate();
				}

			});
		}

	};

	private void maybeUpdate() {
		if (childrenInflated && lastStatus != null)
			update(lastStatus);
	}

	private void update(ServiceStatus status) {
		statusText.setText(status.getSummary());
		toggleButton.setImageResource(status.mode.actionIcon());
		toggleButton.setContentDescription(StringResource.get(status.mode.toggleToOtherModeResourceId()));
	}

}
