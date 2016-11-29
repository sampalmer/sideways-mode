package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;

public class ControlPanelFragment extends Fragment {

	private ServiceStatusContentProvider.Client serviceClient;
	private Handler handler;
	private ServiceStatus lastStatus;
	private ImageButton toggleButton;
	private TextView statusText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serviceClient = new ServiceStatusContentProvider.Client(getActivity());
		handler = new Handler();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_control_panel, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		//TODO: Maybe reuse the code that generates the notification buttons?

		statusText = (TextView) view.findViewById(R.id.control_panel_status);
		ImageButton rotateLeftButton = (ImageButton) view.findViewById(R.id.control_panel_button_rotate_left);
		toggleButton = (ImageButton) view.findViewById(R.id.control_panel_button_toggle);
		ImageButton rotateRightButton = (ImageButton) view.findViewById(R.id.control_panel_button_rotate_right);

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
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		toggleButton = null;
		statusText = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		serviceClient.subscribe(listener);
	}

	@Override
	public void onStop() {
		super.onStop();
		serviceClient.unsubscribe(listener);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (lastStatus != null)
			update(lastStatus);
	}

	private final IServiceStatusListener.Stub listener = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					lastStatus = status;

					if (getActivity() != null)
						update(status);
				}

			});
		}

	};

	private void update(ServiceStatus status) {
		statusText.setText(status.getSummary());
		toggleButton.setImageResource(status.mode.actionIcon());
	}

}
