package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class LoadInstalledAppsTask extends AsyncTask<Void, Integer, ArrayList<InstalledApp>> {

	private ProgressListView list;

	public LoadInstalledAppsTask(ProgressListView v) {
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
	protected void onPostExecute(final ArrayList<InstalledApp> result) {
		super.onPostExecute(null);

		list.adapter.addAll(result);

		//TODO: Maybe move all of this setting code somewhere else?
		final Settings.Setting<Set<String>> setting = ApplicationSettings.remote().forcedOrientationAppPackages();
		Set<String> forcedAppPackages = setting.get();
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

				Set<String> forcedAppPackages = setting.get();
				if (checked)
					forcedAppPackages.add(packageName);
				else
					forcedAppPackages.remove(packageName);
				setting.set(forcedAppPackages);
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
