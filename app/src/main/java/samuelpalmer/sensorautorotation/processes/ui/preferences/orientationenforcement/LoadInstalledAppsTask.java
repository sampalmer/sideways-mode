package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

class LoadInstalledAppsTask extends AsyncTask<Void, Integer, ArrayList<InstalledApp>> {
	
	private final SharedPreferences prefs;
	
	// Only for use in the background thread
	private final PackageManager packageManager;
	
	// Only for use in the UI thread
	private final ProgressListView list;
	
	public LoadInstalledAppsTask(Context context, ProgressListView v) {
		prefs = ApplicationSettings.getSharedPreferences(context);
		packageManager = context.getPackageManager();
		this.list = v;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		list.list.setVisibility(View.GONE);
		list.progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected ArrayList<InstalledApp> doInBackground(Void... params) {
		if (isCancelled())
			return null;

		//Not getting *all* installed packages since it turns out that this can cause a TransactionTooLargeException
		//http://stackoverflow.com/questions/21656440/android-os-transactiontoolargeexception-retrieving-installed-applications
		Set<String> installedPackageNames = new HashSet<>();
		for (String category : new String[] { Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_HOME }) {
			if (isCancelled())
				return null;

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(category);

			List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
			for (ResolveInfo resolveInfo : resolveInfos)
				installedPackageNames.add(resolveInfo.activityInfo.packageName);
		}

		ArrayList<InstalledApp> options = new ArrayList<>();
		for (String installedPackage : installedPackageNames) {
			if (isCancelled())
				return null;

			ApplicationInfo applicationInfo;
			try {
				applicationInfo = packageManager.getApplicationInfo(installedPackage, 0);
			} catch (PackageManager.NameNotFoundException e) {
				Log.w(LoadInstalledAppsTask.class.getSimpleName(), e);
				continue;
			}

			options.add(new InstalledApp(applicationInfo, packageManager));
		}

		Collections.sort(options, new InstalledAppComparator());
		return options;
	}

	@Override
	protected void onPostExecute(final ArrayList<InstalledApp> result) {
		super.onPostExecute(null);

		list.adapter.addAll(result);

		//TODO: Maybe move all of this setting code somewhere else?
		Set<String> forcedAppPackages = prefs.getStringSet(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.<String>emptySet());
		for (int i = 0; i < result.size(); ++i) {
			InstalledApp app = result.get(i);
			if (forcedAppPackages.contains(app.packageName))
				list.list.setItemChecked(i, true);
		}
		
		list.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				boolean checked = ((Checkable)view).isChecked();
				String packageName = result.get(position).packageName;
				
				Set<String> forcedAppPackages = prefs.getStringSet(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.<String>emptySet());
				forcedAppPackages = new HashSet<>(forcedAppPackages); // Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
				
				if (checked)
					forcedAppPackages.add(packageName);
				else
					forcedAppPackages.remove(packageName);
				
				prefs
					.edit()
					.putStringSet(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, forcedAppPackages)
					.apply();
			}
		});

		list.progressBar.setVisibility(View.GONE);
		list.list.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCancelled(ArrayList<InstalledApp> installedApps) {
		list.progressBar.setVisibility(View.GONE);
	}
}
