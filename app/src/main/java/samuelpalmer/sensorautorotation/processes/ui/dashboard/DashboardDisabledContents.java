package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.Screen;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.WindowOrientationListener;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.ui.VerticalTextView;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class DashboardDisabledContents extends LinearLayout {
	
	private ImageButton chooseLeftSideButton;
	private VerticalTextView chooseLeftSideCaption;
	private VerticalTextView chooseRightSideCaption;
	private ImageButton chooseRightSideButton;
	
	private WindowOrientationListener deviceListener;
	private ScreenRotationMonitor screenRotationMonitor;
	
	private boolean isAttachedToWindow = false;
	private boolean running = false;
	
	public DashboardDisabledContents(Context context) {
		super(context);
		init();
	}
	
	public DashboardDisabledContents(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DashboardDisabledContents(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public DashboardDisabledContents(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.dashboard_disabled_contents, this, true);
		
		chooseLeftSideButton = findViewById(R.id.choose_side_left);
		chooseLeftSideCaption = findViewById(R.id.choose_side_left_caption);
		chooseRightSideCaption = findViewById(R.id.choose_side_right_caption);
		chooseRightSideButton = findViewById(R.id.choose_side_right);
		
		chooseLeftSideButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(UserSide.LEFT);
			}
		});
		
		chooseRightSideButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(UserSide.RIGHT);
			}
		});
		
		deviceListener = new WindowOrientationListener(getContext(), new Handler(Looper.getMainLooper())) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				if (rotation < -1 || rotation > 3)
					throw new RuntimeException("Got unknown deviceListener orientation: " + rotation);
				
				somethingChanged();
			}
		};
		//TODO: Add support for checking whether the sensors are working
		
		screenRotationMonitor = new ScreenRotationMonitor(new Screen(SystemServices.windowManager()));
		
		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		chooseLeftSideButton.setImageResource(R.drawable.upright);
		chooseRightSideButton.setImageResource(R.drawable.upright);
		
		updateRunningState();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		isAttachedToWindow = true;
		updateRunningState();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		isAttachedToWindow = false;
		updateRunningState();
	}
	
	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		updateRunningState();
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		updateRunningState();
	}
	
	private void updateRunningState() {
		boolean shouldBeRunning =
			isAttachedToWindow
			&&
			getVisibility() == View.VISIBLE
			&&
			getWindowVisibility() == View.VISIBLE
		;
		
		//TODO: This has a problem: it continues running even if the screen is off (and the activity is therefore stopped)
		//Solution: Somehow hook this up to the activity lifecycle
		
		if (running != shouldBeRunning) {
			if (shouldBeRunning) {
				screenRotationMonitor.subscribe(screenRotationChanged);
				deviceListener.enable();
				
				somethingChanged();
			}
			else {
				screenRotationMonitor.unsubscribe(screenRotationChanged);
				deviceListener.disable();
			}
			
			running = shouldBeRunning;
		}
	}
	
	private final EventHandler screenRotationChanged = new EventHandler() {
		@Override
		public void update() {
			somethingChanged();
		}
	};
	
	private void somethingChanged() {
		int device = deviceListener.getProposedRotation();
		if (device < 0) {
			setRotation(Orientation.NATURAL);
			return;
		}
		
		Orientation screen = screenRotationMonitor.currentRotation();
		
		setRotation(screen.minus(new Rotation(device)));
	}
	
	private void setRotation(Orientation rotation) {
		ViewGroup container = findViewById(R.id.choose_side_container);
		ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
		
		if (rotation.isNaturalOrUpsideDown()) {
			layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
			layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		else {
			layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
		}
		
		setButtonLayout(rotation, chooseLeftSideButton);
		setCaptionLayout(rotation, chooseLeftSideCaption, chooseLeftSideButton);
		setCaptionLayout(rotation.plus(new Rotation(2)), chooseRightSideCaption, chooseRightSideButton);
		setButtonLayout(rotation.plus(new Rotation(2)), chooseRightSideButton);
		
		container.requestLayout();
	}
	
	private void setCaptionLayout(Orientation rotation, VerticalTextView caption, View button) {
		int direction;
		switch (rotation.getClockwiseRotationsFromNaturalOrientation()) {
			case 0:
				direction = VerticalTextView.ORIENTATION_DOWN_TO_UP;
				break;
			case 1:
				direction = VerticalTextView.ORIENTATION_LEFT_TO_RIGHT;
				break;
			case 2:
				direction = VerticalTextView.ORIENTATION_UP_TO_DOWN;
				break;
			case 3:
				direction = VerticalTextView.ORIENTATION_RIGHT_TO_LEFT;
				break;
			default:
				throw new RuntimeException();
		}
		caption.setDirection(direction);
		
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		int verb;
		switch (rotation.getClockwiseRotationsFromNaturalOrientation()) {
			case 0:
				verb = RelativeLayout.RIGHT_OF;
				break;
			case 1:
				verb = RelativeLayout.BELOW;
				break;
			case 2:
				verb = RelativeLayout.LEFT_OF;
				break;
			case 3:
				verb = RelativeLayout.ABOVE;
				break;
			default:
				throw new RuntimeException();
		}
		
		layout.addRule(verb, button.getId());
		caption.setLayoutParams(layout);
	}
	
	private void setButtonLayout(Orientation rotation, View button) {
		button.setRotation(270 + rotation.getClockwiseRotationsFromNaturalOrientation() * 90);
		RelativeLayout.LayoutParams chooseLeftSideButtonLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		int verb;
		switch (rotation.getClockwiseRotationsFromNaturalOrientation()) {
			case 0:
				verb = RelativeLayout.ALIGN_PARENT_LEFT;
				break;
			case 1:
				verb = RelativeLayout.ALIGN_PARENT_TOP;
				break;
			case 2:
				verb = RelativeLayout.ALIGN_PARENT_RIGHT;
				break;
			case 3:
				verb = RelativeLayout.ALIGN_PARENT_BOTTOM;
				break;
			default:
				throw new RuntimeException("Unknown rotation");
		}
		chooseLeftSideButtonLayout.addRule(verb);
		
		button.setLayoutParams(chooseLeftSideButtonLayout);
	}
	
	private void startApp(UserSide userSide) {
		new ServiceSynchroniser(ApplicationSettings.instance()).enableService(userSide);
		
		if (DashboardDialogCoordinator.dialogExists())
			DashboardDialogCoordinator.dismissDialog();
	}
	
}
