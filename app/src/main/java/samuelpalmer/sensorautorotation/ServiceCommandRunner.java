package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.components.RotationService;

public class ServiceCommandRunner {

	private final Context context;
	private final Command command;

	public ServiceCommandRunner(Context context, Command command) {
		this.context = context;
		this.command = command;
	}

	/**
	 * The service must be running before calling this or the service connection will be leaked.
	 * (The service must be explicitly unbound from if the service isn't running.)
	 */
	public void run() {
		Intent intent = new Intent(context, RotationService.class);
		boolean bound = context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
		if (!bound)
			//This seems to happen when the service being bound to isn't installed.
			throw new RuntimeException("Couldn't bind to service");
	}

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder untypedBinder) {
			ServiceApiBinder service = (ServiceApiBinder)untypedBinder;
			command.run(service);
			context.unbindService(this);
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
