package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.*;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IncompatibleAppsChooserActivity extends UiActivity {
	
	private SharedPreferences prefs;
	private InstalledAppComparator installedAppComparator;
	private ProgressListView list;
	private LoadInstalledAppsTask loader;
	
	private boolean needsReload = false;
	private boolean started = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incompatible_apps_chooser);
		
		prefs = ApplicationSettings.getSharedPreferences(this);
		installedAppComparator = new InstalledAppComparator();
		
		list = new ProgressListView();
		list.adapter = new InstalledAppAdapter(this);
		list.list = findViewById(R.id.incompatible_apps_list);
		list.list.setAdapter(list.adapter);
		list.list.setItemsCanFocus(false); //Don't know if this is needed
		list.progressBar = findViewById(R.id.incompatible_apps_progress);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		
		registerReceiver(installedAppsChanged, intentFilter);
		
		beginLoadInstalledApps();
		
		list.list.setOnItemClickListener(onItemClickListener);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		loader.cancel(true);
		loader = null;
		
		unregisterReceiver(installedAppsChanged);
	}
	
	private void beginLoadInstalledApps() {
		loader = new LoadInstalledAppsTask(this, onLoadedListener);
		list.list.setVisibility(View.GONE);
		list.progressBar.setVisibility(View.VISIBLE);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private final BroadcastReceiver installedAppsChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isReplacement = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			if (isReplacement)
				return;
			
			if (started) {
				PackageManager packageManager = getPackageManager();
				
				//noinspection ConstantConditions
				String packageName = intent.getData().getSchemeSpecificPart();
				Set<String> installedPackages = LoadInstalledAppsTask.getInstalledPackages(packageManager, packageName);
				
				boolean shouldBePresent = installedPackages.contains(packageName);
				
				// TODO: Maybe merge this into the adapter and make it a "SortedSetAdapter"?
				InstalledApp matchingItem = null;
				for (int i = 0; i < list.adapter.getCount(); ++i) {
					InstalledApp currentItem = list.adapter.getItem(i);
					assert currentItem != null;
					if (currentItem.packageName.equals(packageName)) {
						matchingItem = currentItem;
						break;
					}
				}
				
				if (shouldBePresent) {
					if (matchingItem == null) {
						InstalledApp installedApp = LoadInstalledAppsTask.getInstalledApp(packageManager, packageName);
						if (installedApp != null) {
							list.adapter.add(installedApp);
							list.adapter.sort(installedAppComparator);
						}
					}
				}
				else {
					if (matchingItem != null)
						list.adapter.remove(matchingItem);
				}
				
				markIncompatibleAppPackages();
			}
			else {
				// The app is in the background, so it might waste resources if we update the UI at this point in time.
				needsReload = true;
			}
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		prefs.registerOnSharedPreferenceChangeListener(onApplicationSettingsChanged);
		
		if (needsReload) {
			loader.cancel(true);
			beginLoadInstalledApps();
			needsReload = false;
		}
		markIncompatibleAppPackages(); // Just in case they changed while the app was in the background
		
		started = true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		prefs.unregisterOnSharedPreferenceChangeListener(onApplicationSettingsChanged);
		
		started = false;
	}
	
	private final LoadInstalledAppsTask.OnLoadedListener onLoadedListener = result -> {
		if (!list.adapter.isEmpty())
			list.adapter.clear();
		
		list.adapter.addAll(result);
		list.adapter.sort(installedAppComparator);
		
		markIncompatibleAppPackages();
		
		list.progressBar.setVisibility(View.GONE);
		list.list.setVisibility(View.VISIBLE);
	};
	
	private final AdapterView.OnItemClickListener onItemClickListener = (parent, view, position, id) -> {
		InstalledApp item = list.adapter.getItem(position);

		SharedPreferences.Editor editor = prefs.edit();

		// Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
		Set<String> incompatibleAppPackages = new HashSet<>(prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet()));
		
		//noinspection ConstantConditions
		if (item.isIncompatible)
			incompatibleAppPackages.remove(item.packageName);
		else {
			if (!prefs.getBoolean(ApplicationSettings.KEY_INCOMPATIBLE_APPS_TIP_SHOWN, false))
				// The user hasn't been shown the tip about this feature, but they're trying the feature out for themselves,
				// so no need to show them the tip anymore.
				editor.putBoolean(ApplicationSettings.KEY_INCOMPATIBLE_APPS_TIP_SHOWN, true);

			incompatibleAppPackages.add(item.packageName);
		}

		editor
			.putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages)
			.apply();
	};
	
	private final SharedPreferences.OnSharedPreferenceChangeListener onApplicationSettingsChanged = (sharedPreferences, key) -> {
		if (key.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
			markIncompatibleAppPackages();
	};
	
	private void markIncompatibleAppPackages() {
		Set<String> incompatibleAppPackages = prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
		assert incompatibleAppPackages != null;

		for (int i = 0; i < list.adapter.getCount(); ++i) {
			InstalledApp item = list.adapter.getItem(i);
			assert item != null;
			item.isIncompatible = incompatibleAppPackages.contains(item.packageName);
		}
		
		list.adapter.notifyDataSetChanged();
	}
	
}
