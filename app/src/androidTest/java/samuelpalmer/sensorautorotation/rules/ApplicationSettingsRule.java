package samuelpalmer.sensorautorotation.rules;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Provides convenient access to change {@link ApplicationSettings}, and resets them after each test.
 */
public class ApplicationSettingsRule extends ExternalResource {

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

        // Resetting to default preference values
        prefs
                .edit()
                .clear()
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

    public void setTurnOffWhenLocked(boolean turnOffWhenLocked) {
        prefs
                .edit()
                .putBoolean(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED, turnOffWhenLocked)
                .commit();
    }

}
