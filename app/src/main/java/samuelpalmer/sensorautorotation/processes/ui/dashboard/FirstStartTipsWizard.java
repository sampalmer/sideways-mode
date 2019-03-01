package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import samuelpalmer.sensorautorotation.R;

class FirstStartTipsWizard {

    private final Dialog dialog;
    private final Listener listener;

    private final float dialogOriginalDimAmount;
    private final TapTarget[] tips;

    private boolean tipsRunning;
    private TapTargetView currentTargetView;
    private int currentTipIndex;

    private View disableActivityView;
    private Context context;
    private WindowManager windowManager;
    private final WindowManager.LayoutParams disableActivityViewParams;

    public interface Listener {
        void onCompleted();
        void onCancelled();
    }

    public FirstStartTipsWizard(Dialog dialog, View sideButton, View menuButton, View offButton, int startAtTipIndex, Listener listener) {
        this.dialog = dialog;
        this.listener = listener;

        //noinspection ConstantConditions
        this.dialogOriginalDimAmount = dialog.getWindow().getAttributes().dimAmount;

        context = dialog.getContext();
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

        tips = new TapTarget[] {
                TapTarget
                        .forView(
                                sideButton,
                                context.getString(R.string.intro_tip_side_button_title),
                                context.getString(R.string.intro_tip_side_button_description, context.getText(R.string.app_name_full))
                        )
                        .icon(getDrawable(sideButton))
                        .drawShadow(true), // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

                TapTarget
                        .forView(
                                menuButton,
                                context.getString(R.string.intro_tip_incompatible_apps_title),
                                context.getString(R.string.intro_tip_incompatible_apps_description)
                        )
                        .drawShadow(true), // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

                TapTarget
                        .forView(
                                offButton,
                                context.getString(R.string.intro_tip_uninstall_title),
                                context.getString(R.string.intro_tip_uninstall_description, context.getText(R.string.app_name_full))
                        )
                        .icon(getDrawable(offButton))
                        .drawShadow(true) // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.
        };

        if (startAtTipIndex < 0 || startAtTipIndex >= tips.length)
            throw new IllegalArgumentException("Tip index out of bounds");

        this.currentTipIndex = startAtTipIndex;
    }

    public void start() {
        if (tipsRunning)
            throw new RuntimeException("Already started");

        tipsRunning = true;

        windowManager.addView(disableActivityView, disableActivityViewParams);

        //noinspection ConstantConditions
        dialog.getWindow().setDimAmount(1f); // Making the background behind the dialog completely black to create a cinematic effect so the user knows they are watching rather than interacting with the app.

        showCurrentTip();
    }

    public void stop() {
        if (!tipsRunning)
            throw new RuntimeException("Already stopped");

        //noinspection ConstantConditions
        dialog.getWindow().setDimAmount(dialogOriginalDimAmount);

        windowManager.removeView(disableActivityView);

        tipsRunning = false;

        if (currentTargetView != null) {
            currentTargetView.dismiss(false);
            currentTargetView = null;
        }
    }

    public boolean isRunning() {
        return tipsRunning;
    }

    public int getCurrentTipIndex() {
        return currentTipIndex;
    }

    /**
     * Gets the first drawable that can be found in the given view hierarchy.
     * This is needed because a selected dashboard button has an underline,
     * which I don't want to include in the popup since it looks weird.
     */
    private Drawable getDrawable(View button) {
        if (button instanceof ImageView) {
            ImageView imageView = (ImageView) button;
            return imageView.getDrawable();
        }
        else if (button instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) button;

            for (int i = 0; i < container.getChildCount(); ++i) {
                View child = container.getChildAt(i);
                Drawable childDrawable = getDrawable(child);
                if (childDrawable != null)
                    return childDrawable;
            }
        }

        return null;
    }

    private void showCurrentTip() {
        if (currentTipIndex >= tips.length) {
            currentTipIndex = 0; // Just in case this gets re-used
            listener.onCompleted();
            stop();
            return;
        }

        TapTarget currentTip = tips[currentTipIndex];

        currentTargetView = TapTargetView.showFor(
                dialog,
                currentTip,
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
                        currentTargetView = null;
                        ++currentTipIndex;
                        showCurrentTip();
                    }

                }
        );
    }

}
