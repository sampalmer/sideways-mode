package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ChooseSideActivity extends DialogActivity {
	
	private Dialog dialog;
	private View chooseLeftSideButton;
	private VerticalTextView chooseLeftSideCaption;
	private VerticalTextView chooseRightSideCaption;
	private View chooseRightSideButton;
	private WindowOrientationListener deviceListener;
	
	private ScreenRotationMonitor screenRotationMonitor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		dialog = new AlertDialog.Builder(this)
			.setView(getLayoutInflater().inflate(R.layout.fragment_choose_side, null))
			.show();
		
		chooseLeftSideButton = dialog.findViewById(R.id.choose_side_left);
		chooseLeftSideCaption = dialog.findViewById(R.id.choose_side_left_caption);
		chooseRightSideCaption = dialog.findViewById(R.id.choose_side_right_caption);
		chooseRightSideButton = dialog.findViewById(R.id.choose_side_right);
		
		
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
		
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				finish();
			}
		});
		
		setRotation(Orientation.LEFT_SIDE);
		
		deviceListener = new WindowOrientationListener(this, new Handler(Looper.getMainLooper())) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				if (rotation < -1 || rotation > 3)
					throw new RuntimeException("Got unknown deviceListener orientation: " + rotation);
				
				somethingChanged();
			}
		};
		//TODO: Add support for checking whether the sensors are working
		
		screenRotationMonitor = new ScreenRotationMonitor(new Screen(SystemServices.windowManager()));
		screenRotationMonitor.subscribe(screenRotationChanged);
		
		deviceListener.enable();
		somethingChanged();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		screenRotationMonitor.unsubscribe(screenRotationChanged);
		screenRotationMonitor = null;
		
		deviceListener.disable();
		deviceListener = null;
		
		Log.i(ChooseSideActivity.class.getSimpleName(), "Destroying");
		
		//Prevents a window leak error
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
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
		
		setRotation(screen.plus(new Rotation(device)));
	}
	
	private void setRotation(Orientation rotation) {
		ViewGroup container = dialog.findViewById(R.id.choose_side_container);
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
	
	
	@Override
	protected void onStop() {
		super.onStop();
		
		/*
		 * Since this activity can be shown on the lock screen,
		 * leaving the activity open when you lock your phone would
		 * result in it being shown when you next unlock your phone.
		 * (At least in Android 7.1.1.)
		 *
		 * Finishing the activity on stop seems to fix this nicely.
		 * Note that this only works because the activity is declared as
		 * supporting screen size, orientation & layout config changes. If it wasn't,
		 * changes to those properties would also result in the activity finishing.
		 */
		finish();
	}
	
	private void startApp(UserSide userSide) {
		new ServiceSynchroniser(ApplicationSettings.instance()).enableService(userSide);
		finish();
	}
	
}
