package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;

class FirstStartTipsWizard {

    private final Handler handler;
    private final DisplayMetrics displayMetrics;
    private final Dialog dialog;
    private final View sideButton;
    private final PopupMenu popupMenu;
    private final Listener listener;

    private boolean tipsRunning;
    private TapTargetView currentTargetView;
    private Runnable currentQueuedItem;
    private View incompatibleAppsTextView;
    private View uninstallTextView;
    private View disableActivityView;
    private Context context;
    private WindowManager windowManager;
    private final WindowManager.LayoutParams disableActivityViewParams;

    public interface Listener {
        void onCompleted();
    }

    public FirstStartTipsWizard(Handler handler, DisplayMetrics displayMetrics, Dialog dialog, View sideButton, PopupMenu popupMenu, Listener listener) {
        this.handler = handler;
        this.displayMetrics = displayMetrics;
        this.dialog = dialog;
        this.sideButton = sideButton;
        this.popupMenu = popupMenu;
        this.listener = listener;

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

        disableActivityView = new View(context);
    }

    public void start() {
        if (tipsRunning)
            throw new RuntimeException("Already started");

        tipsRunning = true;

        windowManager.addView(disableActivityView, disableActivityViewParams);

        step1.run();
    }

    public void stop() {
        if (!tipsRunning)
            throw new RuntimeException("Already stopped");

        windowManager.removeView(disableActivityView);

        tipsRunning = false;
        incompatibleAppsTextView = null;
        uninstallTextView = null;
        popupMenu.dismiss();

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
                    "Lying down",
                    "When you lie down, tell Sideways Mode which side you're on.");

            showTapTarget(sideButtonTarget, () -> queueStep(step2, 500));
        }
    };

    private final Runnable step2 = new Runnable() {
        @Override
        public void run() {
            popupMenu.show();

            // Wait for the popup menu to appear
            queueStep(step3, 1500);
        }
    };

    private final Runnable step3 = new Runnable() {
        @Override
        public void run() {
            Menu menu = popupMenu.getMenu();

            List<View> rootViews = ViewUtilities.getRootViews();

            // TODO: Do something if the root views can't be obtained (= null)

            incompatibleAppsTextView = FirstStartTipsWizard.this.getMenuItemText(rootViews, menu.findItem(R.id.action_incompatible_apps));
            uninstallTextView = FirstStartTipsWizard.this.getMenuItemText(rootViews, menu.findItem(R.id.action_uninstall));

            // TODO: Do something if the menu items aren't found

            // Shrinking menu item containers to fit so the tap target matches the text length
            ViewGroup.LayoutParams layoutParams = incompatibleAppsTextView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            incompatibleAppsTextView.setLayoutParams(layoutParams);

            ViewGroup.LayoutParams layoutParams2 = uninstallTextView.getLayoutParams();
            layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            uninstallTextView.setLayoutParams(layoutParams2);

            // TODO: See if there's a way to bypass the need for this additional callback
            // Need to wait a bit for the above layout change to take effect
            queueStep(step4, 0);
        }
    };

    private final Runnable step4 = new Runnable() {
        @Override
        public void run() {
            TapTarget incompatibleAppsTarget = makeMenuItemTapTarget(
                    displayMetrics,
                    incompatibleAppsTextView,
                    "Use your favourite apps", // TODO: See if you can come up with better wording for the "Use your favourite apps" tip
                    "If an app doesn't rotate correctly, add it here to fix it."
            );

            showTapTarget(incompatibleAppsTarget, ()-> queueStep(step5, 1000));
        }
    };

    private final Runnable step5 = new Runnable() {
        @Override
        public void run() {
            TapTarget uninstallTarget = makeMenuItemTapTarget(
                    displayMetrics,
                    uninstallTextView,
                    "Don't want Sideways Mode?",
                    "If you decide to uninstall Sideways Mode, do it here so the screen doesn't get stuck.");

            showTapTarget(uninstallTarget, () -> {
                listener.onCompleted();
                queueStep(step6, 1000);
            });
        }
    };

    private final Runnable step6 = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    private TapTarget makeMenuItemTapTarget(DisplayMetrics displayMetrics, View textView, String title, String description) {
        int textWidthPx = textView.getWidth();
        float textWidthDp = textWidthPx / displayMetrics.density;
        int textRadiusDp = (int) Math.ceil(textWidthDp / 2.0f) + 8;
        int standardTapTargetRadiusDp = 44;
        int targetRadius = Math.max(textRadiusDp, standardTapTargetRadiusDp);

        return TapTarget
                .forView(textView, title, description)
                .targetRadius(targetRadius);
    }

    private View getMenuItemText(List<View> rootViews, MenuItem menuItem) {
        View targetMenuItemText = null;

        for (View root : rootViews)
            if (targetMenuItemText == null)
                targetMenuItemText = findViewByText(root, menuItem.getTitle());

        return targetMenuItemText;
    }

    private View findViewByText(View view, CharSequence targetText) {
        if (view.getVisibility() == View.VISIBLE && view.getWidth() > 0 && view.getHeight() > 0) // Not interested in views that aren't displayed
            if (view instanceof TextView)
            {
                TextView textView = (TextView) view;
                CharSequence text = textView.getText();

                if (text.equals(targetText))
                    return view;
            }
            else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;

                for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                    View childView = viewGroup.getChildAt(i);
                    View result = findViewByText(childView, targetText);
                    if (result != null)
                        return result;
                }
            }

        return null;
    }

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
        if (delayMillis == 0)
            handler.post(step);
        else
            handler.postDelayed(step, delayMillis);

        currentQueuedItem = step;
    }
}
