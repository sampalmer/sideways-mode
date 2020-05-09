package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.TaskRunner;

public class IncompatibleAppsChooserFragment extends Fragment {
    
    private SharedPreferences prefs;
    private InstalledAppComparator installedAppComparator;
    private ProgressListView list;
    private Future<?> loader;
    
    private boolean needsReload = false;
    private boolean started = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incompatible_apps_chooser, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = ApplicationSettings.getSharedPreferences(requireContext());
        installedAppComparator = new InstalledAppComparator();

        View root = getView();
        assert root != null; // Since onActivityCreated() is called after onCreateView()

        list = new ProgressListView();
        list.adapter = new InstalledAppAdapter(requireContext());
        list.list = root.findViewById(R.id.incompatible_apps_list);
        list.list.setAdapter(list.adapter);
        list.list.setItemsCanFocus(false); // Don't know if this is needed
        list.progressBar = root.findViewById(R.id.incompatible_apps_progress);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");

        requireContext().registerReceiver(installedAppsChanged, intentFilter);

        beginLoadInstalledApps();

        list.list.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loader.cancel(true);
        loader = null;

        requireContext().unregisterReceiver(installedAppsChanged);
    }
    
    private void beginLoadInstalledApps() {
        list.list.setVisibility(View.GONE);
        list.progressBar.setVisibility(View.VISIBLE);

        LoadInstalledAppsTask task = new LoadInstalledAppsTask(requireContext());
        loader = LoadInstalledAppsTask.taskRunner.executeAsync(task, onLoadedListener);
    }
    
    private final BroadcastReceiver installedAppsChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isReplacement = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (isReplacement)
                return;
            
            if (started) {
                PackageManager packageManager = requireContext().getPackageManager();
                
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
    public void onStart() {
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
    public void onStop() {
        super.onStop();
        prefs.unregisterOnSharedPreferenceChangeListener(onApplicationSettingsChanged);
        
        started = false;
    }
    
    private final TaskRunner.Callback<ArrayList<InstalledApp>> onLoadedListener = result -> {
        if (result == null)
            // The task must have been cancelled, so nothing to do here
            return;

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

        for (int i = 0; i < list.adapter.getCount(); ++i) {
            InstalledApp item = list.adapter.getItem(i);
            assert item != null;
            item.isIncompatible = incompatibleAppPackages.contains(item.packageName);
        }
        
        list.adapter.notifyDataSetChanged();
    }
    
}
