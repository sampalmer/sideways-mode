package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class Process extends Application {

	private static final String TAG = Process.class.getSimpleName();
	private static Context context;

	public static Context context() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "Process created");

		context = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "Process terminated");
	}

}
