package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;

public class PopupActivity extends UiActivity {

	private ServiceStatusContentProvider.Client serviceClient;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		setContentView(R.id.)

		serviceClient = new ServiceStatusContentProvider.Client(this);
		handler = new Handler();


		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onStart() {
		super.onStart();
		serviceClient.subscribe(listener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		serviceClient.unsubscribe(listener);
	}

	private final IServiceStatusListener.Stub listener = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					//TODO: Properly implement this.
					Toast.makeText(PopupActivity.this, status.getSummary(), Toast.LENGTH_SHORT).show();
				}

			});
		}

	};
}
