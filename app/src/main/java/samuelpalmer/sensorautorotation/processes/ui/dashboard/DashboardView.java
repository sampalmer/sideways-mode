package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceCommand;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.processes.service.statusinterface.IServiceStatusListener;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class DashboardView extends FrameLayout {

	private final ServiceStatusContentProvider.Client serviceClient;
	private final Handler handler;
	private final Settings.Setting<Boolean> enabledSetting;

	private final boolean showSettings;

	private ViewGroup disabledSection;
	private TextView disabledText;
	private Button disabledViewError;

	private ProgressBar progressBar;
	private ViewGroup contents;
	private ImageView userIcon;
	private TextView statusText;

	private ImageButton toggleModeButton;
	private TextView modeText;
	private boolean childrenInflated;
	private ServiceStatus lastStatus;
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

		serviceClient = new ServiceStatusContentProvider.Client(getContext());
		handler = new Handler();
		enabledSetting = ApplicationSettings.remote().enabled();

		View view = inflate(getContext(), R.layout.dashboard_contents, null);
		addView(view);

		ImageButton settingsButton = (ImageButton) findViewById(R.id.dashboard_settings);
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

		disabledSection = (ViewGroup) findViewById(R.id.dashboard_disabled);
		disabledText = (TextView) findViewById(R.id.dashboard_disabled_text);
		disabledViewError = (Button) findViewById(R.id.dashboard_disabled_view_error);

		progressBar = (ProgressBar) findViewById(R.id.dashboard_progress);
		contents = (ViewGroup) findViewById(R.id.dashboard_contents);

		userIcon = (ImageView) findViewById(R.id.dashboard_user_icon);
		statusText = (TextView) findViewById(R.id.dashboard_status);
		ImageButton rotateLeftButton = (ImageButton) findViewById(R.id.dashboard_button_rotate_left);
		toggleModeButton = (ImageButton) findViewById(R.id.dashboard_button_toggle);
		ImageButton rotateRightButton = (ImageButton) findViewById(R.id.dashboard_button_rotate_right);
		modeText = (TextView) findViewById(R.id.dashboard_mode);

		userIcon.setColorFilter(ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500));

		rotateLeftButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_LEFT)));

		//Not showing the toast when this is pressed since it causes a flickering issue on Samsung Galaxy S5 (Android 6)
		toggleModeButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_TOGGLE)));

		rotateRightButton.setOnClickListener(new RotationServiceClickListener(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_RIGHT)));

		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		settingsButton.setImageResource(R.drawable.menu_settings);
		rotateLeftButton.setImageResource(R.drawable.dashboard_rotate_left);
		rotateRightButton.setImageResource(R.drawable.dashboard_rotate_right);

		childrenInflated = true;
		enabledChanged.update();
		maybeUpdateUi();
	}

	private class RotationServiceClickListener implements View.OnClickListener {

		private final RotationServiceCommand command;

		public RotationServiceClickListener(RotationServiceCommand command) {
			this.command = command;
		}

		@Override
		public void onClick(View v) {
			RotationServiceClient.send(command);
			//Not showing a progress bar here since most of the time the update will be instant, in which case flicker would occur
		}

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

	private final IServiceStatusListener.Stub statusChanged = new IServiceStatusListener.Stub() {

		@Override
		public void onChange(final ServiceStatus status) throws RemoteException {
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

		ensureVisibility(disabledSection, isEnabled ? View.GONE : View.VISIBLE);
		ensureVisibility(progressBar, isLoading ? View.VISIBLE : View.GONE);

		if (isEnabled)
			//Don't want the view to change size after loading; it should stay the same
			ensureVisibility(contents, isLoading ? View.INVISIBLE : View.VISIBLE);
		else
			ensureVisibility(contents, View.GONE);

		if (!isEnabled) {
			final EnvironmentError lastError = EnvironmentError.lastError();

			//Not showing any error details so the user has to view the full error info and actions to reduce chance of user blaming developer and sending useless error reports.
			disabledText.setText(StringResource.format(lastError == null ? R.string.app_disabled_message : R.string.app_disabled_because_of_error, R.string.app_name_full));

			if (lastError == null) {
				disabledViewError.setVisibility(View.GONE);
				disabledViewError.setOnClickListener(null);
			}
			else {
				disabledViewError.setVisibility(View.VISIBLE);
				disabledViewError.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							lastError.showDialogIntent().send();
						} catch (PendingIntent.CanceledException e) {
							throw new RuntimeException(e);
						}

						if (DashboardDialogCoordinator.dialogExists())
							DashboardDialogCoordinator.dismissDialog();
					}
				});
			}
		}
		else {
			if (lastStatus != null) {
				if (lastStatus.waitingForSensor) {
					ensureVisibility(userIcon, View.GONE);
					statusText.setText(R.string.status_waiting_sensor);
				}
				else {
					userIcon.setImageResource(lastStatus.user.iconId());
					userIcon.setContentDescription(StringResource.get(lastStatus.user.descriptionId()));
					ensureVisibility(userIcon, View.VISIBLE);

					statusText.setText(" " + StringResource.get(lastStatus.user.longDescriptionId()));
				}

				toggleModeButton.setImageResource(lastStatus.mode.dashboardIcon());
				toggleModeButton.setContentDescription(StringResource.get(lastStatus.mode.toggleToOtherModeResourceId()));
				modeText.setText(lastStatus.mode.autoRotationStateResourceId());
			}
		}
	}

	/**
	 * Only changes the visibility if it's different to the current value.
	 * This is to minimise lag.
	 */
	private static void ensureVisibility(View control, int newVisibility) {
		int currentVisibility = control.getVisibility();
		if (currentVisibility != newVisibility)
			control.setVisibility(newVisibility);
	}

}
