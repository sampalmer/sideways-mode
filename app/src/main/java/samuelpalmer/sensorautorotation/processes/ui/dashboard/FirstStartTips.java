package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;

public class FirstStartTips extends Fragment {

    private Dialog dialog;
    private View sideButton;
    private PopupMenu popupMenu;

    public void setTargets(Dialog dialog, View sideButton, PopupMenu popupMenu) {
        this.dialog = dialog;
        this.sideButton = sideButton;
        this.popupMenu = popupMenu;
    }

    public void x() {

        // TODO: Maybe don't start the tutorial if the user interacts with the activity before the tutorial starts
        // TODO: In each callback, check that everything is still in a valid state before proceeding
        // TODO: See if you can come up with better wording for the "Use your favourite apps" tip
        // TODO: Make sure this stuff works ok when the activity is destroyed and recreated (eg screen orientation change)

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: Make the entire UI non-interactive during the tutorial
                // TODO: Make sure the activity is actually available at this point in time
                TapTarget leftButtonTarget = TapTarget.forView(
                        sideButton,
                        "Lying down",
                        "When you lie down, tell Sideways Mode which side you're on.");

                TapTargetView.showFor(dialog, leftButtonTarget, new TapTargetView.Listener() {
                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);
                        proceed();
                    }

                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        proceed();
                    }

                    private void proceed() {
                        new Handler().postDelayed(() -> {
                            popupMenu.show();

                            // Wait for the popup menu to appear
                            new Handler().postDelayed(() -> {
                                Menu menu = popupMenu.getMenu();

                                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                                List<View> rootViews = ViewUtilities.getRootViews();

                                // TODO: Do something if the root views can't be obtained (= null)

                                View incompatibleAppsTextView = getMenuItemText(rootViews, menu.findItem(R.id.action_incompatible_apps));
                                View uninstallTextView = getMenuItemText(rootViews, menu.findItem(R.id.action_uninstall));

                                // TODO: Do something if the menu items aren't found

                                // Shrinking menu item containers to fit so the tap target matches the text length
                                ViewGroup.LayoutParams layoutParams = incompatibleAppsTextView.getLayoutParams();
                                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                                incompatibleAppsTextView.setLayoutParams(layoutParams);

                                ViewGroup.LayoutParams layoutParams2 = uninstallTextView.getLayoutParams();
                                layoutParams2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                                uninstallTextView.setLayoutParams(layoutParams2);

                                // Need to wait a bit for the above layout change to take effect
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        TapTarget incompatibleAppsTarget = makeMenuItemTapTarget(
                                                displayMetrics,
                                                incompatibleAppsTextView,
                                                "Use your favourite apps",
                                                "If an app doesn't rotate correctly, add it here to fix it.");


                                        TapTarget uninstallTarget = makeMenuItemTapTarget(
                                                displayMetrics,
                                                uninstallTextView,
                                                "Don't want Sideways Mode?",
                                                "If you decide to uninstall Sideways Mode, do it here so the screen doesn't get stuck.");

                                        TapTargetView.showFor(dialog, incompatibleAppsTarget, new TapTargetView.Listener() {
                                            public void onTargetCancel(TapTargetView view) {
                                                super.onTargetCancel(view);
                                                proceed();
                                            }

                                            @Override
                                            public void onTargetClick(TapTargetView view) {
                                                super.onTargetClick(view);
                                                proceed();
                                            }

                                            private void proceed() {
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        TapTargetView.showFor(dialog, uninstallTarget, new TapTargetView.Listener() {
                                                            public void onTargetCancel(TapTargetView view) {
                                                                super.onTargetCancel(view);
                                                                proceed();
                                                            }

                                                            @Override
                                                            public void onTargetClick(TapTargetView view) {
                                                                super.onTargetClick(view);
                                                                proceed();
                                                            }

                                                            private void proceed() {
                                                                new Handler().postDelayed(() -> {
                                                                    popupMenu.dismiss();
                                                                }, 1000);
                                                            }
                                                        });
                                                    }
                                                }, 1000);
                                            }
                                        });
                                    }
                                });
                            }, 1500);
                        }, 500);
                    }
                });
            }
        }, 2000);
    }


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

}
