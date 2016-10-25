package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;

public class ControlPanelFragment extends Fragment {

	private ServiceStatusContentProvider.Client serviceClient;
	private Handler handler;
	private ServiceStatus lastStatus;

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
		//TODO: Properly implement this.
		Toast.makeText(getActivity(), status.getSummary(), Toast.LENGTH_SHORT).show();
	}

}
