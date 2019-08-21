package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadInstalledAppsTask extends AsyncTask<Void, Integer, ArrayList<InstalledApp>> {
    
    // Only for use in the background thread
    private final PackageManager packageManager;
    
    private final OnLoadedListener loadedListener;
    
    public LoadInstalledAppsTask(Context context, OnLoadedListener loadedListener) {
        packageManager = context.getPackageManager();
        this.loadedListener = loadedListener;
    }
    
    /**
     * @param packageName If set, will limit the results to this package only.
     */
    @NonNull
    public static Set<String> getInstalledPackages(PackageManager packageManager, @Nullable String packageName) {
        // Not getting *all* installed packages since it turns out that this can cause a TransactionTooLargeException
        // http://stackoverflow.com/questions/21656440/android-os-transactiontoolargeexception-retrieving-installed-applications
        Set<String> installedPackageNames = new HashSet<>();
        for (String category : new String[] { Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_HOME }) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).setPackage(packageName);
            mainIntent.addCategory(category);
            
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo resolveInfo : resolveInfos)
                installedPackageNames.add(resolveInfo.activityInfo.packageName);
        }
        
        return installedPackageNames;
    }
    
    /**
     * @return {@code null} if the package isn't found.
     */
    @Nullable
    public static InstalledApp getInstalledApp(PackageManager packageManager, String packageName) {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(LoadInstalledAppsTask.class.getSimpleName(), e);
            return null;
        }
        
        return  new InstalledApp(applicationInfo, packageManager, false);
    }
    
    @Override
    protected ArrayList<InstalledApp> doInBackground(Void... params) {
        if (isCancelled())
            return null;

        Set<String> installedPackageNames = getInstalledPackages(packageManager, null);

        ArrayList<InstalledApp> options = new ArrayList<>();
        for (String installedPackage : installedPackageNames) {
            if (isCancelled())
                return null;
            
            InstalledApp installedApp = getInstalledApp(packageManager, installedPackage);
            if (installedApp != null)
                options.add(installedApp);
        }
        
        return options;
    }

    @Override
    protected void onPostExecute(final ArrayList<InstalledApp> result) {
        super.onPostExecute(null);
        loadedListener.loaded(result);
    }
    
    public interface OnLoadedListener {
        void loaded(ArrayList<InstalledApp> result);
    }
    
}
