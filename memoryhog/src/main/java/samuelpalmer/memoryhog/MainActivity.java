package samuelpalmer.memoryhog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;


public class MainActivity extends Activity {
	
	private static final int NUMBER_OF_PROCESSES = 8;
	
	@SuppressWarnings("unused")
	private MemoryHog memoryHog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		Log.i(getClass().getSimpleName(), "Activity created. Consuming memory.");
		memoryHog = new MemoryHog(this);
		for (Class s : getServiceClasses())
			startService(new Intent(getApplicationContext(), s));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(getClass().getSimpleName(), "Activity destroyed. Releasing memory.");
		for (Class s : getServiceClasses())
			stopService(new Intent(getApplicationContext(), s));
		memoryHog = null;
	}
	
	private ArrayList<Class<?>> getServiceClasses() {
		ServiceInfo[] serviceInfos = getServiceInfos();
		ArrayList<Class<?>> results = new ArrayList<>();
		
		for (ServiceInfo service : serviceInfos)
			if (results.size() < NUMBER_OF_PROCESSES)
				results.add(loadClass(service));
		
		if (results.size() < NUMBER_OF_PROCESSES)
			throw new RuntimeException("Requested " + NUMBER_OF_PROCESSES + " processes, but only found " + results.size() + " services.");
		
		return results;
	}

	private ServiceInfo[] getServiceInfos() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SERVICES).services;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Class<?> loadClass(ServiceInfo service) {
		try {
			return getClassLoader().loadClass(new ComponentName(service.packageName, service.name).getClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
