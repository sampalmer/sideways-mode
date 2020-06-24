package samuelpalmer.sensorautorotation.rules;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class IncompatibleAppsRule extends ExternalResource {

    private SharedPreferences prefs;

    @Override
    protected void before() throws Throwable {
        super.before();

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefs = ApplicationSettings.getSharedPreferences(targetContext);
    }

    @Override
    protected void after() {
        super.after();

        prefs
                .edit()
                .putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, null)
                .commit();
        prefs = null;
    }

    public void markAsIncompatible(String appPackage) {
        Set<String> originalIncompatiblePackages = prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
        Set<String> newIncompatibleAppPackages = new HashSet<>(originalIncompatiblePackages); // Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
        newIncompatibleAppPackages.add(appPackage);

        prefs
                .edit()
                .putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, newIncompatibleAppPackages)
                .commit();
    }

}
