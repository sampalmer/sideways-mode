package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusService;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public class DashboardView extends FrameLayout {
	
	private ApplicationStatusService.Client serviceClient;
	private final Settings.Setting<UserSide> servicePersistentState;
	
	private final DashboardPermissionContents permissionSection;
	private DashboardDisabledContents disabledSection;
	private ProgressBar progressBar;
	private DashboardEnabledContents enabledSection;
	private boolean childrenInflated;
	private ApplicationStatus lastStatus;
	private boolean isEnabled;
	
	public DashboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		servicePersistentState = ServiceSettings.instance().persistentState();

		View view = inflate(getContext(), R.layout.dashboard, null);
		addView(view);

		ImageButton settingsButton = findViewById(R.id.dashboard_settings);
		settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getContext().startActivity(new Intent(getContext(), MainSettingsActivity.class));

				if (DashboardDialogCoordinator.dialogExists())
					DashboardDialogCoordinator.dismissDialog();
			}
		});

		permissionSection = findViewById(R.id.dashboard_permission);
		disabledSection = findViewById(R.id.dashboard_disabled_contents);
		progressBar = findViewById(R.id.dashboard_progress);
		enabledSection = findViewById(R.id.dashboard_enabled_contents);

		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		settingsButton.setImageResource(R.drawable.menu_settings);

		childrenInflated = true;
		enabledChanged.update();
		maybeUpdateUi();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		boolean boundSuccessfully = getContext().bindService(
			new Intent(getContext(), ApplicationStatusService.class),
			serviceConnection,
			Context.BIND_AUTO_CREATE
		);
		
		if (!boundSuccessfully) {
			// The documentation says you should still unbind after a binding failure
			getContext().unbindService(serviceConnection);
			throw new RuntimeException("Couldn't connect to " + ApplicationStatusService.class.getSimpleName());
		}
		
		servicePersistentState.subscribe(enabledChanged);
		EnvironmentError.onChange.subscribe(lastErrorChanged);

		enabledChanged.update();
		maybeUpdateUi();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		if (serviceClient != null)
			try {
				serviceClient.unsubscribe();
			} catch (RemoteException ignored) {
				// The service process died, but that doesn't matter since we're unsubscribing.
			}
		getContext().unbindService(serviceConnection);
		
		servicePersistentState.unsubscribe(enabledChanged);
		EnvironmentError.onChange.unsubscribe(lastErrorChanged);
	}
	
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceClient = new ApplicationStatusService.Client(iBinder, statusChanged);
			
			try {
				serviceClient.subscribe();
			} catch (RemoteException e) {
				// Service process died, so we'll have to try again once it's back up.
				serviceClient = null;
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceClient = null;
		}
		
	};
	
	
	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		
		// This is needed so we can re-check if the "modify system settings" permission has been granted
		maybeUpdateUi();
	}
	
	private final ApplicationStatusService.Client.Listener statusChanged = new ApplicationStatusService.Client.Listener() {

		@Override
		public void onChange(final ApplicationStatus status) {
			lastStatus = status;
			maybeUpdateUi();
		}

	};

	private final EventHandler enabledChanged = new EventHandler() {
		@Override
		public void update() {
			isEnabled = servicePersistentState.hasValue();
			if (!isEnabled)
				lastStatus = null; //So we don't accidentally reuse the last status next time the app is enabled

			maybeUpdateUi();
		}
	};

	private final EventHandler lastErrorChanged = new EventHandler() {
		@Override
		public void update() {
			maybeUpdateUi();
		}
	};

	private void maybeUpdateUi() {
		if (childrenInflated)
			showStatus();
	}

	@SuppressLint("SetTextI18n")
	private void showStatus() {
		boolean isLoading = isEnabled && lastStatus == null;
		
		ViewHelpers.ensureVisibility(progressBar, isLoading ? View.VISIBLE : View.GONE);
		
		if (!isEnabled) {
			ViewHelpers.ensureVisibility(enabledSection, View.GONE);
			
			boolean hasPermission = Build.VERSION.SDK_INT < 23 || android.provider.Settings.System.canWrite(getContext());
			ViewHelpers.ensureVisibility(disabledSection, hasPermission ? View.VISIBLE : View.GONE);
			ViewHelpers.ensureVisibility(permissionSection, hasPermission ? View.GONE : View.VISIBLE);
		}
		else {
			//Don't want the view to change size after loading; it should stay the same
			ViewHelpers.ensureVisibility(enabledSection, isLoading ? View.INVISIBLE : View.VISIBLE);
		
			if (lastStatus != null)
				enabledSection.setStatus(lastStatus);
			
			ViewHelpers.ensureVisibility(disabledSection, View.GONE);
			ViewHelpers.ensureVisibility(permissionSection, View.GONE);
		}
	}
	
}
