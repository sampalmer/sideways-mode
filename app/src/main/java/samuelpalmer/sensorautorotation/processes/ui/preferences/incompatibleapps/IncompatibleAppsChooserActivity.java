package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

//TODO: Keep the list of installed apps up-to-date (the user might install, uninstall, disable, or enable apps)
public class IncompatibleAppsChooserActivity extends UiActivity {
	
	private SharedPreferences prefs;
	private ProgressListView list;
	private LoadInstalledAppsTask loader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incompatible_apps_chooser);
		
		prefs = ApplicationSettings.getSharedPreferences(this);
		
		list = new ProgressListView();
		list.adapter = new InstalledAppAdapter(this);
		list.list = findViewById(R.id.incompatible_apps_list);
		list.list.setAdapter(list.adapter);
		list.list.setItemsCanFocus(false); //Don't know if this is needed
		list.progressBar = findViewById(R.id.incompatible_apps_progress);
		
		loader = new LoadInstalledAppsTask(this, onLoadedListener);
		
		list.list.setVisibility(View.GONE);
		list.progressBar.setVisibility(View.VISIBLE);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		list.list.setOnItemClickListener(onItemClickListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loader.cancel(true);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		prefs.registerOnSharedPreferenceChangeListener(onApplicationSettingsChanged);
		markIncompatibleAppPackages(); // Just in case they changed while the app was in the background
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		prefs.unregisterOnSharedPreferenceChangeListener(onApplicationSettingsChanged);
	}
	
	private final LoadInstalledAppsTask.OnLoadedListener onLoadedListener = result -> {
		list.adapter.addAll(result);
		
		markIncompatibleAppPackages();
		
		list.progressBar.setVisibility(View.GONE);
		list.list.setVisibility(View.VISIBLE);
	};
	
	private final AdapterView.OnItemClickListener onItemClickListener = (parent, view, position, id) -> {
		InstalledApp item = list.adapter.getItem(position);
		
		// Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
		Set<String> incompatibleAppPackages = new HashSet<>(prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet()));
		
		//noinspection ConstantConditions
		if (item.isIncompatible)
			incompatibleAppPackages.remove(item.packageName);
		else
			incompatibleAppPackages.add(item.packageName);
		
		prefs
			.edit()
			.putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages)
			.apply();
	};
	
	private final SharedPreferences.OnSharedPreferenceChangeListener onApplicationSettingsChanged = (sharedPreferences, key) -> {
		if (key.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
			markIncompatibleAppPackages();
	};
	
	private void markIncompatibleAppPackages() {
		Set<String> incompatibleAppPackages = prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
		
		for (int i = 0; i < list.adapter.getCount(); ++i) {
			InstalledApp item = list.adapter.getItem(i);
			assert item != null;
			item.isIncompatible = incompatibleAppPackages.contains(item.packageName);
		}
		
		list.adapter.notifyDataSetChanged();
	}
	
}
