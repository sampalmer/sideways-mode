package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps.IncompatibleAppsActivity;
import samuelpalmer.sensorautorotation.utilities.ViewUtilities;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

public class DashboardDialogFragment extends AppCompatDialogFragment {

    private static final String TAG_NORMAL_CONTENTS_FRAGMENT = "normalContents";
    private static final String TAG_PERMISSION_CONTENTS_FRAGMENT = "permissionContents";
    private static final String TAG_TIPS_FRAGMENT = "tips";

    private DashboardNormalFragment normalSection;
    private DashboardPermissionFragment permissionSection;
    private FirstStartTipsFragment tipsFragment;
    private ImageButton menuButton;
    private PopupMenu popup;
    private EventRegistrar permissionMonitor;

    public DashboardDialogFragment() {
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public int getTheme() {
        TypedValue outValue = new TypedValue();
        Context context = getContext();
        assert context != null;
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        menuButton = view.findViewById(R.id.dashboard_menu);
        assert menuButton != null;
        menuButton.setOnClickListener(v -> popup.show());

        popup = new PopupMenu(getActivity(), menuButton);
        popup.setOnMenuItemClickListener(this::handleMenuItemClick);
        popup.inflate(R.menu.main);
        menuButton.setOnTouchListener(popup.getDragToOpenListener());

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        normalSection = (DashboardNormalFragment) fragmentManager.findFragmentByTag(TAG_NORMAL_CONTENTS_FRAGMENT);
        if (normalSection == null) {
            normalSection = new DashboardNormalFragment();
            fragmentTransaction.add(R.id.dashboard_normal_contents, normalSection, TAG_NORMAL_CONTENTS_FRAGMENT);
        }

        permissionSection = (DashboardPermissionFragment) fragmentManager.findFragmentByTag(TAG_PERMISSION_CONTENTS_FRAGMENT);
        if (permissionSection == null) {
            permissionSection = new DashboardPermissionFragment();
            fragmentTransaction.add(R.id.dashboard_permission_contents, permissionSection, TAG_PERMISSION_CONTENTS_FRAGMENT);
        }

        tipsFragment = (FirstStartTipsFragment) fragmentManager.findFragmentByTag(TAG_TIPS_FRAGMENT);
        if (tipsFragment == null) {
            tipsFragment = new FirstStartTipsFragment();
            fragmentTransaction.add(tipsFragment, TAG_TIPS_FRAGMENT);
        }

        fragmentTransaction.commit();

        tipsFragment.setStatus(normalSection.getStatus());
        normalSection.setStatusChangeListener(status -> tipsFragment.setStatus(status));

        return view;
    }

    public void setCommandSentListener(DashboardNormalFragment.CommandSentListener commandSentListener) {
        normalSection.setCommandSentListener(commandSentListener);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // It's important that we finish the activity when the dialog is *cancelled*, not dismissed.
        // This is because dismissal also occurs when the screen orientation or layout changes.
        // Hence if we finished the activity on *dismiss*, the dialog would disappear each time the screen orientation changes.
        getActivity().finish();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionMonitor = ScreenOrientationController.makePermissionMonitor(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        permissionMonitor = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        tipsFragment.setTargets(getDialog(), normalSection.getRightSideButton(), menuButton, normalSection.getOffButton());

        permissionMonitor.subscribe(permissionChanged);
        checkPermission();

        tipsFragment.onCanShowTipsChanged.subscribe(updateShowTipsState);
        updateShowTipsState.update();
    }

    @Override
    public void onStop() {
        super.onStop();

        tipsFragment.onCanShowTipsChanged.unsubscribe(updateShowTipsState);
        permissionMonitor.unsubscribe(permissionChanged);
    }

    private final EventHandler updateShowTipsState = new EventHandler() {
        @Override
        public void update() {
            Menu menu = popup.getMenu();
            MenuItem tipsMenuItem = menu.findItem(R.id.action_tips);
            boolean canShowTips = tipsFragment.canShowTips();
            tipsMenuItem.setEnabled(canShowTips);
        }
    };

    private final EventHandler permissionChanged = this::checkPermission;

    private void checkPermission() {
        boolean hasPermission = ScreenOrientationController.hasPermissionToStart(getContext());
        ViewUtilities.ensureVisibility(normalSection.getView(), hasPermission ? View.VISIBLE : View.GONE);
        ViewUtilities.ensureVisibility(permissionSection.getView(), hasPermission ? View.GONE : View.VISIBLE);
        tipsFragment.setNormalSectionVisibility(hasPermission);
    }

    @SuppressWarnings("SameReturnValue")
    private boolean handleMenuItemClick(MenuItem menuItem) {
        /*
            The dashboard activity needs to run in its own task that does not show up in the recents screen so it feels like a dialog rather than a full activity.
            However, the activities we launch from the dashboard should have normal activity behaviour, so we launch them into a separate task.
         */
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;
        
        switch (menuItem.getItemId()) {
            case R.id.action_tips: {
                tipsFragment.showAllTips();
                break;
            }
            case R.id.action_incompatible_apps: {
                Intent intent = new Intent(getActivity(), IncompatibleAppsActivity.class).addFlags(flags);
                startActivity(intent);
                break;
            }
            default:
                throw new RuntimeException("Unknown navigation menu item: " + menuItem.getItemId());
        }
        
        return true;
    }

}
