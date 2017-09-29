package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.common.NumberUtilities;
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

public class DashboardDisabledContents extends LinearLayout {
	
	private TextView chooseLeftSideCaption;
	private TextView chooseRightSideCaption;
	
	private WindowOrientationListener deviceListener;
	private ScreenRotationMonitor screenRotationMonitor;
	
	private boolean isAttachedToWindow = false;
	private boolean running = false;
	private ObjectAnimator animator;
	private View buttonContainer;
	
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

		chooseLeftSideCaption = findViewById(R.id.choose_side_left_caption);
		chooseRightSideCaption = findViewById(R.id.choose_side_right_caption);

		chooseLeftSideCaption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(UserSide.LEFT);
			}
		});

		chooseRightSideCaption.setOnClickListener(new View.OnClickListener() {
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
		Drawable arrow = ResourcesCompat.getDrawable(getResources(), R.drawable.upright, null);
		chooseLeftSideCaption.setCompoundDrawablesWithIntrinsicBounds(null, arrow, null, null);
		chooseRightSideCaption.setCompoundDrawablesWithIntrinsicBounds(null, arrow, null, null);
		
		buttonContainer = findViewById(R.id.choose_side_buttons_container);
		animator = ObjectAnimator
			.ofFloat(buttonContainer, "rotation", 0)
			.setDuration(200);

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
		if (device >= 0) {
			Orientation screen = screenRotationMonitor.currentRotation();
			setRotation(screen.minus(new Rotation(device)));
		}
	}
	
	private void setRotation(Orientation rotation) {
		float currentRotation = NumberUtilities.wrap(buttonContainer.getRotation(), 360f);
		float targetRotation = NumberUtilities.wrap((rotation.getClockwiseRotationsFromNaturalOrientation() + 1f) * -90f, 360f);
		
		// Always take the shortest path
		if (targetRotation - currentRotation > 180f)
			currentRotation += 360f;
		else if (currentRotation - targetRotation > 180f)
			currentRotation -= 360f;
		
		animator.setFloatValues(currentRotation, targetRotation);
		animator.start();
	}
	
	private void startApp(UserSide userSide) {
		ServiceSynchroniser.enableService(userSide);
		
		if (DashboardDialogCoordinator.dialogExists())
			DashboardDialogCoordinator.dismissDialog();
	}
	
}
