package samuelpalmer.sensorautorotation;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;

//TODO: Pause this on screen off
//TODO: Figure out how to prevent apps that require enforcement from launching in their own orientations
//TODO: Consider using broadcasts or some other concept that might be more suitable
public class OrientationEnforcingMonitor extends Thread {

	private final ActivityManager activityManager;
	private final Context context;
	private final Handler uiThread;
	private final String currentHomePackage;
	private ScreenOrientationEnforcer enforcer; //Must be accessed by UI thread.

    public OrientationEnforcingMonitor(Context context){
		this.context = context;
        activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		uiThread = new Handler(context.getMainLooper());
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		currentHomePackage = resolveInfo.activityInfo.packageName;
		
		start();
    }

    public void run(){
        while (true) {
            updateEnforcement();
    		pause();
        }
    }

	private void updateEnforcement() {
		Runnable action = shouldEnforce() ? enforce : dontEnforce;
		uiThread.post(action);
	}

	private boolean shouldEnforce() {
		final String packageName = getCurrentActivityPackageName();
		return packageName.equals(currentHomePackage);
	}

	private final Runnable enforce = new Runnable() {

		@Override
		public void run() {
			if (enforcer == null)
				enforcer = new ScreenOrientationEnforcer(context);
		}
	};
	
	private final Runnable dontEnforce = new Runnable() {
	
		@Override
		public void run() {
			if (enforcer != null) {
				enforcer.close();
				enforcer = null;
			}
		}
	};
	
	private String getCurrentActivityPackageName() {
		List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
		RunningTaskInfo task = tasks.get(0);
		ComponentName mainActivity = task.baseActivity;
		return mainActivity.getPackageName();
	}

    private void pause() {
		try {
			Thread.sleep(333);
		} catch (InterruptedException e) {
		}
	}
}
