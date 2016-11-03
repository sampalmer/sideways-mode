package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import samuelpalmer.sensorautorotation.SystemServices;

public class LoadInstalledAppsTask extends AsyncTask<Void, Integer, Collection<InstalledApp>> {

	private ProgressListView list;

	public LoadInstalledAppsTask(ProgressListView v) {
		this.list = v;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		list.progressBar.setIndeterminate(true);
		list.list.setVisibility(View.GONE);
		list.progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected Collection<InstalledApp> doInBackground(Void... params) {
		PackageManager packageManager = SystemServices.packageManager();

		if (isCancelled())
			return null;

		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);

		ArrayList<InstalledApp> options = new ArrayList<>();
		for (PackageInfo installedPackage : installedPackages)
			if (shouldShow(installedPackage)) {
				if (isCancelled())
					return null;

				options.add(new InstalledApp(installedPackage, packageManager));
			}

		Collections.sort(options, new InstalledAppComparator());
		return options;
	}

	private boolean shouldShow(PackageInfo installedPackage) {
		//TODO: Maybe ignore disabled activities? And non-exported ones?

		return
			installedPackage.activities != null
			&&
			installedPackage.activities.length > 0
			&&
			installedPackage.applicationInfo != null
			&&
			installedPackage.applicationInfo.enabled;
	}

	@Override
	protected void onPostExecute(Collection<InstalledApp> result) {
		super.onPostExecute(null);

		list.adapter.addAll(result);
		list.progressBar.setVisibility(View.GONE);
		list.list.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCancelled(Collection<InstalledApp> installedApps) {
		list.progressBar.setVisibility(View.GONE);
	}
}
