package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppPackagesPreference extends MultiSelectListPreference {

	private final PackageManager packageManager;

	public AppPackagesPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		packageManager = context.getPackageManager();

		initialiseOptions();
	}

	private void initialiseOptions() {
		ArrayList<InstalledApp> options = lookupOptions();

		ArrayList<CharSequence> availableOptionDisplayText = new ArrayList<>(options.size());
		ArrayList<CharSequence> availableValues = new ArrayList<>(options.size());
		for (InstalledApp app : options) {
			availableOptionDisplayText.add(app.label);
			availableValues.add(app.packageName);
		}

		setEntries(availableOptionDisplayText.toArray(new CharSequence[availableOptionDisplayText.size()]));
		setEntryValues(availableValues.toArray(new CharSequence[availableValues.size()]));
	}

	private ArrayList<InstalledApp> lookupOptions() {
		List<PackageInfo> installedPackages = packageManager
				.getInstalledPackages(PackageManager.GET_ACTIVITIES);

		ArrayList<PackageInfo> appsWithActivities = new ArrayList<>();
		for (PackageInfo installedPackage : installedPackages)
			if (installedPackage.activities != null && installedPackage.activities.length > 0)
				appsWithActivities.add(installedPackage);

		ArrayList<InstalledApp> options = new ArrayList<>();
		for (PackageInfo app : appsWithActivities)
			options.add(new InstalledApp(app));

		Collections.sort(options, new Comparator<InstalledApp>() {
			@Override
			public int compare(InstalledApp lhs, InstalledApp rhs) {
				return lhs.label.toString().compareToIgnoreCase(rhs.label.toString());
			}
		});
		return options;
	}

	class InstalledApp {

		private final CharSequence packageName;
		private final CharSequence label;

		public InstalledApp(PackageInfo app) {
			packageName = app.packageName;
			label = app.applicationInfo.loadLabel(packageManager);
		}

	}

}
