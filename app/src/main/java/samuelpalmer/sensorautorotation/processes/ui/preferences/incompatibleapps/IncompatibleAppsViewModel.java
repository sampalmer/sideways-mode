package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * View-model for {@link IncompatibleAppsFragment}
 */
@SuppressWarnings("WeakerAccess")
public class IncompatibleAppsViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> incompatibleAppCount = new MutableLiveData<>();
    private SharedPreferences prefs;

    public LiveData<Integer> getIncompatibleAppCount() {
        return incompatibleAppCount;
    }

    public IncompatibleAppsViewModel(@NonNull Application application) {
        super(application);
        prefs = ApplicationSettings.getSharedPreferences(application);
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        updateCount();

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        prefs = null;
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (prefs != null)
                if (key == null || key.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
                    updateCount();
        }
    };

    private void updateCount() {
        Set<String> incompatibleApps = prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
        int quantity = incompatibleApps.size();
        incompatibleAppCount.setValue(quantity);
    }

}
