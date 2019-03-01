package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.WindowCallbackWrapper;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class FirstStartTipsFragment extends Fragment {

    private static final String STATE_DIALOG_TOUCHED = "dialogTouched";
    private static final String STATE_SHOWING_TIPS = "showingTips";
    private static final String STATE_CURRENT_TIP_INDEX = "currentTipIndex";

    private Handler handler;
    private Dialog dialog;
    private View sideButton;
    private View menuButton;
    private View offButton;

    private ApplicationStatus status;
    private SharedPreferences applicationSettings;
    private boolean normalSectionVisible;

    private boolean dialogTouched;
    private boolean isActiveActivity;
    private boolean waiting;
    private boolean showingTips;
    private int currentTipIndex;
    private FirstStartTipsWizard tips;
    private boolean started;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        if (savedInstanceState != null) {
            dialogTouched = savedInstanceState.getBoolean(STATE_DIALOG_TOUCHED);
            showingTips = savedInstanceState.getBoolean(STATE_SHOWING_TIPS);
            currentTipIndex = savedInstanceState.getInt(STATE_CURRENT_TIP_INDEX);
        }

        somethingChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dialog != null && dialog.getWindow() != null)
            // Unwrapping the window callback since we don't need it anymore
            dialog.getWindow().setCallback(dialog);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_DIALOG_TOUCHED, dialogTouched);
        outState.putBoolean(STATE_SHOWING_TIPS, showingTips);
        outState.putInt(STATE_CURRENT_TIP_INDEX, tips == null ? currentTipIndex : tips.getCurrentTipIndex());
    }

    public void setTargets(Dialog dialog, View sideButton, View menuButton, View offButton) {
        this.dialog = dialog;
        this.sideButton = sideButton;
        this.menuButton = menuButton;
        this.offButton = offButton;

        dialog.getWindow().setCallback(new WindowCallbackWrapper(dialog) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent event) {
                if (status != null && normalSectionVisible)
                    // Don't care about touches while the dialog isn't ready
                    dialogTouched = true;

                somethingChanged();
                return super.dispatchTouchEvent(event);
            }
        });

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
        somethingChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        applicationSettings = null;
        somethingChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        started = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        started = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActiveActivity = true;
        somethingChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        isActiveActivity = false;
        somethingChanged();
    }

    private void somethingChanged() {
        boolean shouldShowTips =
                dialog != null
                        &&
                        sideButton != null
                        &&
                        menuButton != null
                        &&
                        offButton != null
                        &&
                        status != null
                        &&
                        normalSectionVisible
                        &&
                        applicationSettings != null
                        &&
                        !applicationSettings.getBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, false);

        boolean shouldWait =
                shouldShowTips
                        &&
                        isActiveActivity
                        &&
                        !showingTips
                        &&
                        !dialogTouched;

        if (shouldWait != waiting) {
            if (shouldWait) {
                handler.postDelayed(showTips, 3000);
                waiting = true;
            } else {
                handler.removeCallbacks(showTips);
                waiting = false;
            }
        }

        if (showingTips && shouldShowTips && started && tips == null)
            // The fragment might have been destroyed, in which case we'll need to recreate the wizard.
            showTips.run();
    }

    private final Runnable showTips = new Runnable() {
        @Override
        public void run() {
            tips = new FirstStartTipsWizard(
                    dialog,
                    sideButton,
                    menuButton,
                    offButton,
                    currentTipIndex,
                    new FirstStartTipsWizard.Listener() {
                        @Override
                        public void onCompleted() {
                            proceed();
                        }

                        @Override
                        public void onCancelled() {
                            proceed();
                        }

                        private void proceed() {
                            applicationSettings
                                    .edit()
                                    .putBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, true)
                                    .apply();

                            tips = null;
                            showingTips = false;
                        }

                    }
            );

            tips.start();

            showingTips = true;
        }
    };

}
