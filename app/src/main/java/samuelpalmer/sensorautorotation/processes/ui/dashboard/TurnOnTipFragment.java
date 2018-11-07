package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.WindowCallbackWrapper;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

/**
 * Shows the user how to turn on the app if needed.
 */
public class TurnOnTipFragment extends Fragment {

    private static final String STATE_DIALOG_TOUCHED = "dialogTouched";
    private static final String STATE_SHOWING_TIP = "showingTip";

    private Handler handler;
    private Dialog dialog;
    private View sideButton;

    private ApplicationStatus status;
    private SharedPreferences applicationSettings;
    private boolean normalSectionVisible;

    private boolean dialogTouched;
    private boolean isActiveActivity;
    private boolean waiting;
    private boolean showingTip;
    private TapTargetView tip;
    private boolean started;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        if (savedInstanceState != null) {
            dialogTouched = savedInstanceState.getBoolean(STATE_DIALOG_TOUCHED);
            showingTip = savedInstanceState.getBoolean(STATE_SHOWING_TIP);
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
        outState.putBoolean(STATE_SHOWING_TIP, showingTip);
    }

    public void setTargets(Dialog dialog, View sideButton) {
        this.dialog = dialog;
        this.sideButton = sideButton;

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
        boolean canShowTip =
                dialog != null
                        &&
                        sideButton != null
                        &&
                        status != null
                        &&
                        normalSectionVisible
                        &&
                        applicationSettings != null
                        &&
                        !applicationSettings.getBoolean(ApplicationSettings.KEY_TURNING_ON_TIP_SHOWN, false);

        boolean shouldWait =
                canShowTip
                        &&
                        isActiveActivity
                        &&
                        !showingTip
                        &&
                        !dialogTouched;

        if (shouldWait != waiting) {
            if (shouldWait) {
                handler.postDelayed(showTip, 3000);
                waiting = true;
            } else {
                handler.removeCallbacks(showTip);
                waiting = false;
            }
        }

        if (showingTip && canShowTip && started && tip == null)
            // The fragment might have been destroyed, in which case we'll need to recreate the tip.
            showTip.run();
    }

    private final Runnable showTip = new Runnable() {
        @Override
        public void run() {
            Context context = getContext();
            assert context != null;

            TapTarget sideButtonTarget = TapTarget.forView(
                    sideButton,
                    context.getString(R.string.tip_turning_on_title),
                    context.getString(R.string.tip_turning_on_description, context.getText(R.string.app_name_full)))
                    .drawShadow(true); // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

            tip = TapTargetView.showFor(
                    dialog,
                    sideButtonTarget,
                    new TapTargetView.Listener() {

                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            proceed();
                        }

                        @Override
                        public void onTargetCancel(TapTargetView view) {
                            super.onTargetCancel(view);
                            proceed();
                        }

                        private void proceed() {
                            applicationSettings
                                    .edit()
                                    .putBoolean(ApplicationSettings.KEY_TURNING_ON_TIP_SHOWN, true)
                                    .apply();

                            tip = null;
                            showingTip = false;
                        }

                    }
            );

            showingTip = true;
        }
    };
}
