package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatus;
import samuelpalmer.sensorautorotation.processes.service.ApplicationStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IApplicationStatusListener;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class DashboardView extends FrameLayout {

	private final ApplicationStatusContentProvider.Client serviceClient;
	private final Handler handler;
	private final Settings.Setting<Boolean> enabledSetting;

	private final boolean showSettings;

	private DashboardDisabledContents disabledSection;

	private ProgressBar progressBar;
	private DashboardEnabledContents contents;
	private boolean childrenInflated;
	private ApplicationStatus lastStatus;
	private boolean isEnabled;

	public DashboardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.DashboardView,
				0, 0);

		try {
			showSettings = a.getBoolean(R.styleable.DashboardView_showSettings, true);
		} finally {
			a.recycle();
		}

		serviceClient = new ApplicationStatusContentProvider.Client(getContext());
		handler = new Handler(Looper.getMainLooper());
		enabledSetting = ApplicationSettings.instance().enabled();

		View view = inflate(getContext(), R.layout.dashboard, null);
		addView(view);

		ImageButton settingsButton = findViewById(R.id.dashboard_settings);
		if (showSettings)
			settingsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getContext().startActivity(new Intent(getContext(), MainActivity.class));

					if (DashboardDialogCoordinator.dialogExists())
						DashboardDialogCoordinator.dismissDialog();
				}
			});
		else
			settingsButton.setVisibility(View.GONE);

		disabledSection = findViewById(R.id.dashboard_disabled);

		progressBar = findViewById(R.id.dashboard_progress);
		contents = findViewById(R.id.dashboard_enabled_contents);

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

		serviceClient.subscribe(statusChanged);
		enabledSetting.subscribe(enabledChanged);
		EnvironmentError.onChange.subscribe(lastErrorChanged);

		enabledChanged.update();
		maybeUpdateUi();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		serviceClient.unsubscribe(statusChanged);
		enabledSetting.unsubscribe(enabledChanged);
		EnvironmentError.onChange.unsubscribe(lastErrorChanged);
	}

	private final IApplicationStatusListener.Stub statusChanged = new IApplicationStatusListener.Stub() {

		@Override
		public void onChange(final ApplicationStatus status) throws RemoteException {
			handler.post(new Runnable() {

				@Override
				public void run() {
					lastStatus = status;
					maybeUpdateUi();
				}

			});
		}

	};

	private final EventHandler enabledChanged = new EventHandler() {
		@Override
		public void update() {
			isEnabled = enabledSetting.get();
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

		ViewHelpers.ensureVisibility(disabledSection, isEnabled ? View.GONE : View.VISIBLE);
		ViewHelpers.ensureVisibility(progressBar, isLoading ? View.VISIBLE : View.GONE);

		if (!isEnabled) {
			ViewHelpers.ensureVisibility(contents, View.GONE);
		}
		else {
			//Don't want the view to change size after loading; it should stay the same
			ViewHelpers.ensureVisibility(contents, isLoading ? View.INVISIBLE : View.VISIBLE);
			
			if (lastStatus != null)
				contents.setStatus(lastStatus);
		}
	}
	
}
