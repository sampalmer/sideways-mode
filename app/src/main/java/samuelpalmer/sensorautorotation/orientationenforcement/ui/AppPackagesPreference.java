package samuelpalmer.sensorautorotation.orientationenforcement.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: Add support for choosing the home activity
public class AppPackagesPreference extends MultiSelectListPreference {

	private final PackageManager packageManager;

	public AppPackagesPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		packageManager = context.getPackageManager();

		setEntries(new CharSequence[]{});
		setEntryValues(new CharSequence[]{});
	}

	@Override
	protected void onClick() {
		initialiseOptions();
		super.onClick();
	}

	//TODO: Load the installed apps in a background AsyncTask or similar
	//TODO: Include app icons
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

		ArrayList<InstalledApp> options = new ArrayList<>();
		for (PackageInfo installedPackage : installedPackages)
			if (shouldShow(installedPackage))
				options.add(new InstalledApp(installedPackage));

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

	public class InstalledApp {

		public final CharSequence packageName;
		public final CharSequence label;

		public InstalledApp(PackageInfo app) {
			packageName = app.packageName;
			label = app.applicationInfo.loadLabel(packageManager);
		}

	}

}
