package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.UserSide;

public class DashboardDisabledContents extends LinearLayout {
	
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
		
		configureUserSideButton(R.id.choose_side_left_caption, UserSide.LEFT, R.drawable.app_left_side);
		configureUserSideButton(R.id.choose_side_right_caption, UserSide.RIGHT, R.drawable.app_right_side);
	}
	
	private void configureUserSideButton(int buttonId, final UserSide side, int iconId) {
		TextView button = findViewById(buttonId);
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(side);
			}
		});
		
		/*
			All image drawables must be set in app code to avoid crashes since:
			1. We're using the vector compat stuff
			2. When this view is inflated by the quick setting service, any images defined in the XML aren't loaded
		 */
		Drawable icon = ResourcesCompat.getDrawable(getResources(), iconId, null);
		
		//noinspection ConstantConditions
		icon.setColorFilter(ResourcesCompat.getColor(getResources(), R.color.primary_500, null), PorterDuff.Mode.MULTIPLY);
		
		button.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
	}
	
	private void startApp(UserSide userSide) {
		ServiceSynchroniser.enableService(userSide);
		
		if (DashboardDialogCoordinator.dialogExists())
			DashboardDialogCoordinator.dismissDialog();
	}
	
}
