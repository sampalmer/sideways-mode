package samuelpalmer.sensorautorotation.values;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationCommandReceiver;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.BroadcastKillFix;

/**
 * Provides a way to remotely communicate with the back-end.
 */
public class ApplicationCommand {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ ACTION_LEFT_SIDE, ACTION_RIGHT_SIDE, ACTION_TURN_OFF, ACTION_CLEAR_ERROR, ACTION_SYNC_CONFIG })
    public @interface ACTION {}
    public static final String ACTION_LEFT_SIDE = "left_side";
    public static final String ACTION_RIGHT_SIDE = "right_side";
    public static final String ACTION_TURN_OFF = "turn_off";
    public static final String ACTION_CLEAR_ERROR = "clear_error";
    public static final String ACTION_SYNC_CONFIG = "sync_config";

    private static final String EXTRA_CONFIG = "config";

    private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
            ACTION_LEFT_SIDE,
            ACTION_RIGHT_SIDE,
            ACTION_TURN_OFF,
            ACTION_CLEAR_ERROR,
            ACTION_SYNC_CONFIG
    ));
    
    public final String action;
    @Nullable public final Config config;

    public ApplicationCommand(@ACTION String action) {
        this(action, (Config)null);
    }

    /**
     * @param applicationSettings If specified, the app setting values will be included in the command for synchronisation with the service settings.
     */
    public ApplicationCommand(@ACTION String action, SharedPreferences applicationSettings) {
        this(
            action,
            new Config(applicationSettings)
        );
    }

    private ApplicationCommand(@ACTION String action, @Nullable Config config) {
        if (action == null || !knownActions.contains(action))
            throw new IllegalArgumentException("Unknown action: " + action);

        this.action = action;
        this.config = config;
    }

    public ApplicationCommand(Intent intent) {
        this(
            intent.getAction(),
            Config.fromBundle(intent.getBundleExtra(EXTRA_CONFIG))
        );
    }

    public void send(Context context) {
        Intent intent = asIntent(context);
        context.sendBroadcast(intent);
    }
    
    public Intent asIntent(Context context) {
        // Not using startService() to deliver commands in case that was the cause of Android having multiple simultaneous instances of the service.
        Intent result = new Intent(context, ApplicationCommandReceiver.class).setAction(action);
        if (config != null)
            result.putExtra(EXTRA_CONFIG, config.asBundle());

        BroadcastKillFix.apply(result);
        return result;
    }

    public static final class Config {
        public final Set<String> incompatibleAppPackages;
        public final boolean turnOffWhenLocked;
        public final boolean closeNotificationWhenUpright;

        public static boolean isSyncableAppSetting(String appSettingKey) {
            return appSettingKey.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES)
                    || appSettingKey.equals(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED)
                    || appSettingKey.equals(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT);
        }

        private Config(SharedPreferences applicationSettings) {
            this(
                applicationSettings.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet()),
                applicationSettings.getBoolean(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED, true),
                applicationSettings.getBoolean(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT, true)
            );
        }

        private Config(Set<String> incompatibleAppPackages, boolean turnOffWhenLocked, boolean closeNotificationWhenUpright) {
            this.incompatibleAppPackages = incompatibleAppPackages;
            if (incompatibleAppPackages == null)
                throw new IllegalArgumentException("incompatibleAppPackages is null");

            this.turnOffWhenLocked = turnOffWhenLocked;
            this.closeNotificationWhenUpright = closeNotificationWhenUpright;
        }

        private static @Nullable Config fromBundle(@Nullable Bundle bundle) {
            if (bundle == null)
                return null;

            Set<String> incompatibleAppPackages = getIncompatibleAppPackages(bundle);

            if (!bundle.containsKey(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED))
                throw new IllegalArgumentException("Bundle is missing value for " + ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED);

            boolean turnOffWhenLocked = bundle.getBoolean(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED);
            boolean closeNotificationWhenUpright = bundle.getBoolean(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT);

            return new Config(incompatibleAppPackages, turnOffWhenLocked, closeNotificationWhenUpright);
        }

        private static Set<String> getIncompatibleAppPackages(Bundle bundle) {
            String[] array = bundle.getStringArray(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES);
            if (array == null)
                return null;

            HashSet<String> result = new HashSet<>(array.length);
            Collections.addAll(result, array);
            return result;
        }

        private Bundle asBundle() {
            Bundle result = new Bundle();
            result.putStringArray(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages.toArray(new String[0]));
            result.putBoolean(ApplicationSettings.KEY_TURN_OFF_WHEN_LOCKED, turnOffWhenLocked);
            result.putBoolean(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT, closeNotificationWhenUpright);
            return result;
        }
    }
    
}
