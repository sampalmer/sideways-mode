package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceCommand;


public class DashboardEnabledContents extends LinearLayout {
	
	public DashboardEnabledContents(Context context) {
		super(context);
		init();
	}
	
	public DashboardEnabledContents(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DashboardEnabledContents(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public DashboardEnabledContents(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	private ImageView userIcon;
	private View sensorProgress;
	private TextView statusText;
	private ImageButton rotateLeftButton;
	private ImageButton toggleModeButton;
	private ImageButton rotateRightButton;
	private TextView modeText;
	
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.dashboard_enabled_contents, this, true);
		
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_HORIZONTAL);
		
		userIcon = (ImageView) findViewById(R.id.dashboard_user_icon);
		sensorProgress = findViewById(R.id.dashboard_sensor_progress);
		
		statusText = (TextView) findViewById(R.id.dashboard_status);
		rotateLeftButton = (ImageButton) findViewById(R.id.dashboard_button_rotate_left);
		toggleModeButton = (ImageButton) findViewById(R.id.dashboard_button_toggle);
		rotateRightButton = (ImageButton) findViewById(R.id.dashboard_button_rotate_right);
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
		rotateLeftButton.setImageResource(R.drawable.dashboard_rotate_left);
		rotateRightButton.setImageResource(R.drawable.dashboard_rotate_right);
	}
	
	private static class RotationServiceClickListener implements View.OnClickListener {
		
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
	
	public void setStatus(ServiceStatus lastStatus) {
		switch (lastStatus.deviceStatus) {
			case WAITING_FOR_SENSOR:
				ViewHelpers.ensureVisibility(userIcon, View.GONE);
				ViewHelpers.ensureVisibility(sensorProgress, View.VISIBLE);
				
				statusText.setText(R.string.status_waiting_sensor);
				break;
			case WAITING_FOR_ORIENTATION:
				userIcon.setImageResource(lastStatus.user.iconId());
				userIcon.setContentDescription(StringResource.get(lastStatus.user.descriptionId()));
				ViewHelpers.ensureVisibility(userIcon, View.VISIBLE);
				ViewHelpers.ensureVisibility(sensorProgress, View.GONE);
				
				statusText.setText(R.string.status_waiting_orientation);
				break;
			case OK:
				userIcon.setImageResource(lastStatus.user.iconId());
				userIcon.setContentDescription(StringResource.get(lastStatus.user.descriptionId()));
				ViewHelpers.ensureVisibility(userIcon, View.VISIBLE);
				ViewHelpers.ensureVisibility(sensorProgress, View.GONE);
				
				statusText.setText(lastStatus.user.longDescriptionId());
				break;
			default:
				throw new RuntimeException("Unsupported device status: " + lastStatus.deviceStatus);
		}
		
		rotateLeftButton.setEnabled(lastStatus.canChangeUserOrientation());
		rotateLeftButton.setAlpha(lastStatus.canChangeUserOrientation() ? 1f : .5f);
		
		toggleModeButton.setImageResource(lastStatus.mode.dashboardIcon());
		toggleModeButton.setContentDescription(StringResource.get(lastStatus.mode.toggleToOtherModeResourceId()));
		modeText.setText(lastStatus.mode.autoRotationStateResourceId());
		
		rotateRightButton.setEnabled(lastStatus.canChangeUserOrientation());
		rotateRightButton.setAlpha(lastStatus.canChangeUserOrientation() ? 1f : .5f);
	}
}
