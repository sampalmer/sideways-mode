package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.*;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import samuelpalmer.sensorautorotation.R;

class FirstStartTipsWizard {

    private final Handler handler;
    private final Dialog dialog;
    private final View sideButton;
    private final View menuButton;
    private final View offButton;
    private final Listener listener;

    private final float dialogOriginalDimAmount;

    private boolean tipsRunning;
    private TapTargetView currentTargetView;
    private Runnable currentQueuedItem;
    private View disableActivityView;
    private Context context;
    private WindowManager windowManager;
    private final WindowManager.LayoutParams disableActivityViewParams;

    public interface Listener {
        void onCompleted();
        void onCancelled();
    }

    public FirstStartTipsWizard(Handler handler, Dialog dialog, View sideButton, View menuButton, View offButton, Listener listener) {
        this.handler = handler;
        this.dialog = dialog;
        this.sideButton = sideButton;
        this.menuButton = menuButton;
        this.offButton = offButton;
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

        disableActivityView.setOnClickListener(v -> {
            // The wizard feels completely unresponsive if you're in a hurry, so we'll skip any pauses whenever the user taps on the app.
            if (tipsRunning && currentQueuedItem != null) {
                handler.removeCallbacks(currentQueuedItem);
                currentQueuedItem.run();
            }
        });
    }

    public void start() {
        if (tipsRunning)
            throw new RuntimeException("Already started");

        tipsRunning = true;

        windowManager.addView(disableActivityView, disableActivityViewParams);

        //noinspection ConstantConditions
        dialog.getWindow().setDimAmount(1f); // Making the background behind the dialog completely black to create a cinematic effect so the user knows they are watching rather than interacting with the app.

        step1.run();
    }

    public void stop() {
        if (!tipsRunning)
            throw new RuntimeException("Already stopped");

        //noinspection ConstantConditions
        dialog.getWindow().setDimAmount(dialogOriginalDimAmount);

        windowManager.removeView(disableActivityView);

        tipsRunning = false;

        if (currentQueuedItem != null) {
            handler.removeCallbacks(currentQueuedItem);
            currentQueuedItem = null;
        }

        if (currentTargetView != null) {
            currentTargetView.dismiss(false);
            currentTargetView = null;
        }
    }

    public boolean isRunning() {
        return tipsRunning;
    }

    private final Runnable step1 = new Runnable() {
        @Override
        public void run() {
            TapTarget sideButtonTarget = TapTarget.forView(
                    sideButton,
                    context.getString(R.string.intro_tip_side_button_title),
                    context.getString(R.string.intro_tip_side_button_description, context.getText(R.string.app_name_full))
            )
                .icon(getDrawable(sideButton))
                .drawShadow(true); // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

            showTapTarget(sideButtonTarget, () -> queueStep(step2, 0));
        }
    };

    private final Runnable step2 = new Runnable() {
        @Override
        public void run() {
            TapTarget incompatibleAppsTarget = TapTarget.forView(
                    menuButton,
                    context.getString(R.string.intro_tip_incompatible_apps_title),
                    context.getString(R.string.intro_tip_incompatible_apps_description)
            )
                .drawShadow(true); // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

            showTapTarget(incompatibleAppsTarget, () -> queueStep(step3, 0));
        }
    };

    private final Runnable step3 = new Runnable() {
        @Override
        public void run() {
            TapTarget incompatibleAppsTarget = TapTarget.forView(
                    offButton,
                    context.getString(R.string.intro_tip_uninstall_title),
                    context.getString(R.string.intro_tip_uninstall_description, context.getText(R.string.app_name_full))
            )
                .icon(getDrawable(offButton))
                .drawShadow(true); // The shadow looks nice and helps distinguish the circle from whatever is behind the dialog.

            showTapTarget(incompatibleAppsTarget, () -> {
                listener.onCompleted();
                queueStep(step4, 0);
            });
        }
    };

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

    private final Runnable step4 = this::stop;

    private void showTapTarget(TapTarget target, Runnable onDismiss) {
        currentTargetView = TapTargetView.showFor(
                dialog,
                target,
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
                        onDismiss.run();
                    }

                }
        );
    }

    private void queueStep(Runnable step, long delayMillis) {
        Runnable wrappedStep = () -> {
            currentQueuedItem = null;
            step.run();
        };

        if (delayMillis == 0)
            handler.post(wrappedStep);
        else
            handler.postDelayed(wrappedStep, delayMillis);

        currentQueuedItem = wrappedStep;
    }
}
