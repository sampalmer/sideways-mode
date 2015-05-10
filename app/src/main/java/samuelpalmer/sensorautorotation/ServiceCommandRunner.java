package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceCommandRunner {

	private final Command command;
	private final ServiceBinder serviceBinder;

	public ServiceCommandRunner(Context context, Command command) {
		serviceBinder = new ServiceBinder(context, connection);
		this.command = command;
	}

	/**
	 * The service must be running before calling this or the service connection will be leaked.
	 * (The service must be explicitly unbound from if the service isn't running.)
	 */
	public void run() {
		serviceBinder.bind();
	}

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder untypedBinder) {
			ServiceApiBinder service = (ServiceApiBinder)untypedBinder;
			command.run(service);
			serviceBinder.unbind();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//Service is already unbound when first connected, so no need to unbind here.
		}
	};

	public interface Command {
		void run(ServiceApiBinder service);
	}

}
