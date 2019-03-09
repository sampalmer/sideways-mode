package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.utilities.WindowCallbackWrapper;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class FirstStartTipsFragment extends Fragment {

    private static final String STATE_DIALOG_TOUCHED = "dialogTouched";
    private static final String STATE_SHOWING_TIPS = "showingTips";

    private Handler handler;
    private Dialog dialog;

    private ApplicationStatus status;
    private boolean normalSectionVisible;

    private boolean dialogTouched;
    private boolean isActiveActivity;
    private boolean waiting;
    private boolean showingTips;
    private FirstStartTipsWizard tips;
    private boolean started;

    public final UpdatingEventRegistrar onCanShowTipsChanged = new UpdatingEventRegistrar();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        if (savedInstanceState != null) {
            dialogTouched = savedInstanceState.getBoolean(STATE_DIALOG_TOUCHED);
            showingTips = savedInstanceState.getBoolean(STATE_SHOWING_TIPS);
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
    }

    public void setTargets(Dialog dialog, View sideButton, View menuButton, View offButton) {
        this.dialog = dialog;

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

        tips = new FirstStartTipsWizard(
                dialog,
                sideButton,
                menuButton,
                offButton,
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
                        showingTips = false;
                    }
                }
        );

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
        boolean shouldWait = canShowTips()
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

        if (showingTips)
            if (canShowTips()) {
                if (started && !tips.isRunning()) {
                    // The fragment might have been destroyed, in which case we'll need to recreate the wizard.
                    showTips.run();
                }
            }
            else {
                if (tips != null && tips.isRunning()) {
                    // In rare cases, something might change mid-way through the wizard.
                    // For example, in split-screen view, the user could revoke the app's permissions.
                    // In these cases, we need to abort the wizard since it will be showing out-of-context.
                    tips.stop();
                    tips = null;
                    showingTips = false;
                }
            }

        onCanShowTipsChanged.report();
    }

    public boolean canShowTips() {
        return dialog != null
                &&
                tips != null
                &&
                status != null
                &&
                normalSectionVisible;
    }

    private final Runnable showTips = () -> showTips(false);

    public void showAllTips() {
        if (!canShowTips())
            throw new IllegalStateException("Cannot show tips at this time");

        showTips(true);
    }

    private void showTips(boolean showAll) {
        tips.start(showAll);
        showingTips = true;
    }

}
