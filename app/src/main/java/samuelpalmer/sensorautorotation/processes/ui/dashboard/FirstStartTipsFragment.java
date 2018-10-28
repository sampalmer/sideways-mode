package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class FirstStartTipsFragment extends Fragment {

    private ApplicationStatus status;
    private SharedPreferences applicationSettings;
    private boolean normalSectionVisible;
    private FirstStartTipsWizard wizard;
    private DisplayMetrics displayMetrics;
    private Dialog dialog;
    private View sideButton;
    private PopupMenu popupMenu;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    public void setTargets(Dialog dialog, View sideButton, PopupMenu popupMenu) {
        this.dialog = dialog;
        this.sideButton = sideButton;
        this.popupMenu = popupMenu;
        somethingChanged();
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        somethingChanged();
    }

    public void setNormalSectionVisibility(boolean isVisible) {
        this.normalSectionVisible = isVisible;
        somethingChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationSettings = ApplicationSettings.getSharedPreferences(context);
        displayMetrics = context.getResources().getDisplayMetrics();
        somethingChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        displayMetrics = null;
        applicationSettings = null;
        somethingChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        somethingChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        somethingChanged();
    }

    private void somethingChanged() {
        if (wizard == null && wizardCanExist())
            wizard = new FirstStartTipsWizard(
                    handler,
                    displayMetrics,
                    dialog,
                    sideButton,
                    popupMenu,
                    () -> {
                        applicationSettings
                                .edit()
                                .putBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, true)
                                .apply();

                        somethingChanged();
                    }
            );

        if (wizard != null) {
            if (shouldStart())
                wizard.start();

            if (wizard.isRunning() && !canRunTips())
                wizard.stop();

            if (!wizardCanExist()) {
                if (wizard.isRunning())
                    wizard.stop();

                wizard = null;
            }
        }
    }

    private boolean wizardCanExist() {
        return handler != null && displayMetrics != null && dialog != null && sideButton != null && popupMenu != null;
    }

    private boolean shouldStart() {
        return
            // TODO: Uncomment the following
//            applicationSettings != null
//            &&
//            !applicationSettings.getBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, false)
//            &&
            !wizard.isRunning()
            &&
            canRunTips()
        ;
}

    private boolean canRunTips() {
        return
            isResumed()
            &&
            normalSectionVisible
            &&
            status != null
        ;
    }

}
