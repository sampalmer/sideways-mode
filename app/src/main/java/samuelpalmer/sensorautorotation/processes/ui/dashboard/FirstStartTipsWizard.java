package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.ThemeUtilities;

class FirstStartTipsWizard {

    private final Dialog dialog;
    private boolean showAllTips;
    private final Listener listener;

    private final float dialogOriginalDimAmount;
    private final Tip[] tips;
    private final SharedPreferences sharedPreferences;

    private boolean tipsRunning;
    private TapTargetView currentTargetView;
    private int currentTipIndex;

    private final View disableActivityView;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams disableActivityViewParams;

    public interface Listener {
        void onCompleted();
        void onCancelled();
    }

    public FirstStartTipsWizard(Dialog dialog, View sideButton, View menuButton, View offButton, Listener listener) {
        this.dialog = dialog;
        this.listener = listener;

        //noinspection ConstantConditions
        this.dialogOriginalDimAmount = dialog.getWindow().getAttributes().dimAmount;

        Context context = dialog.getContext();

        sharedPreferences = ApplicationSettings.getSharedPreferences(context);
        windowManager = ContextCompat.getSystemService(context, WindowManager.class);

        disableActivityViewParams = new WindowManager.LayoutParams();
        disableActivityViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        disableActivityViewParams.format = PixelFormat.TRANSPARENT;
        disableActivityViewParams.alpha = 0.0f;
        disableActivityViewParams.gravity = Gravity.START | Gravity.TOP;
        disableActivityViewParams.x = 0;
        disableActivityViewParams.y = 0;
        disableActivityViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        disableActivityViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        disableActivityView = new View(context) {

            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    event.startTracking();
                    return true;
                }

                return onKeyDown(keyCode, event);
            }

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
                    listener.onCancelled();
                    return true;
                }

                return super.onKeyUp(keyCode, event);
            }

        };

        tips = new Tip[] {
                new Tip(TapTarget
                        .forView(
                                sideButton,
                                context.getString(R.string.intro_tip_side_button_title),
                                context.getString(R.string.intro_tip_side_button_description, context.getText(R.string.app_name_full))
                        ),
                        ApplicationSettings.KEY_TURNING_ON_TIP_SHOWN
                ),

                new Tip(TapTarget
                        .forView(
                                menuButton,
                                context.getString(R.string.intro_tip_incompatible_apps_title),
                                context.getString(R.string.intro_tip_incompatible_apps_description, context.getString(R.string.preference_incompatible_apps_title))
                        ),
                        ApplicationSettings.KEY_INCOMPATIBLE_APPS_TIP_SHOWN
                ),

                new Tip(TapTarget
                        .forView(
                                offButton,
                                context.getString(R.string.intro_tip_uninstall_title),
                                context.getString(R.string.intro_tip_uninstall_description, context.getText(R.string.app_name_full))
                        ),
                        ApplicationSettings.KEY_UNINSTALLING_TIP_SHOWN
                ),
        };
    }

    public void start(boolean showAll) {
        if (tipsRunning)
            throw new RuntimeException("Already started");

        this.showAllTips = showAll;

        if (!hasTipsToShow()) {
            // This is needed to prevent UI lag during user interaction
            listener.onCompleted();
            return;
        }

        tipsRunning = true;

        windowManager.addView(disableActivityView, disableActivityViewParams);

        // Unfortunately, changing the dim doesn't work prior to Android 4.3 since the OS doesn't support changing it after it's already been initialised
        if (Build.VERSION.SDK_INT >= 18) {
            //noinspection ConstantConditions
            dialog.getWindow().setDimAmount(1f); // Making the background behind the dialog completely black to create a cinematic effect so the user knows they are watching rather than interacting with the app.
        }

        showCurrentTip();
    }

    public void stop() {
        if (!tipsRunning)
            throw new RuntimeException("Already stopped");

        if (Build.VERSION.SDK_INT >= 18) {
            //noinspection ConstantConditions
            dialog.getWindow().setDimAmount(dialogOriginalDimAmount);
        }

        windowManager.removeView(disableActivityView);

        tipsRunning = false;

        if (currentTargetView != null) {
            currentTargetView.dismiss(false);
            currentTargetView = null;
        }
    }

    private boolean hasTipsToShow() {
        for (Tip tip : tips)
            if (!tip.isShown())
                return true;

        return false;
    }

    public boolean isRunning() {
        return tipsRunning;
    }

    private void showCurrentTip() {
        if (currentTipIndex >= tips.length) {
            currentTipIndex = 0; // Just in case this gets re-used
            listener.onCompleted();
            stop();
            return;
        }

        Tip currentTip = tips[currentTipIndex];

        if (currentTip.isShown()) {
            ++currentTipIndex;
            showCurrentTip();
            return;
        }

        currentTargetView = TapTargetView.showFor(
                dialog,
                currentTip.tapTarget,
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
                        currentTip.markAsShown();
                        currentTargetView = null;
                        ++currentTipIndex;
                        showCurrentTip();
                    }

                }
        );
    }

    private class Tip {

        private final TapTarget tapTarget;
        private final String shownPreferenceKey;

        private Tip(TapTarget tapTarget, String shownPreferenceKey) {
            this.tapTarget = tapTarget;
            this.shownPreferenceKey = shownPreferenceKey;

            tapTarget.drawShadow(true); // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

            int uiMode = dialog.getContext().getResources().getConfiguration().uiMode;
            boolean isDark = (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            if (isDark) {
                // The library doesn't work well with dark mode out-of-the-box

                @ColorInt int primaryColour = ThemeUtilities.getThemeColour(dialog.getContext(), R.attr.colorPrimary);

                // Using 24% primary as suggested at https://material.io/design/color/dark-theme.html#states
                @ColorInt int outerCircleColour = ColorUtils.blendARGB(Color.BLACK, primaryColour, 0.24f);

                tapTarget
                        .outerCircleColorInt(outerCircleColour) // Defaults to primary colour, which is too bright and glary for dark mode
                        .textColorInt(Color.WHITE) // Defaults to black, which is way too dark for dark mode
                        .targetCircleColorInt(primaryColour) // Defaults to black, which doesn't draw the user's attention to the target
                ;
            } else {
                tapTarget.tintTarget(false); // The default tinted grey icon has poor contrast, so disabling tinting
            }
        }

        private boolean isShown() {
            return !showAllTips && sharedPreferences.getBoolean(shownPreferenceKey, false);
        }

        private void markAsShown() {
            sharedPreferences
                    .edit()
                    .putBoolean(shownPreferenceKey, true)
                    .apply();
        }
    }

}
