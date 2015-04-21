package samuelpalmer.sensorautorotation.orientationenforcement.currentapplicationretrieval;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

public abstract class CurrentApplicationRetriever {

	protected final ActivityManager activityManager;

	public static CurrentApplicationRetriever create(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		if (Build.VERSION.SDK_INT < 21)
			return new PreLollipopCurrentApplicationRetriever(activityManager);
		else
			return new LollipopCurrentApplicationRetriever(activityManager);
	}

	CurrentApplicationRetriever(ActivityManager activityManager) {
		this.activityManager = activityManager;
	}

	public abstract String[] getPackages();

}
