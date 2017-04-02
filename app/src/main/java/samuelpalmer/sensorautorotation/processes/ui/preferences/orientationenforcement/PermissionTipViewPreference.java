package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;


public class PermissionTipViewPreference extends Preference {

	private Button grantPermissionsButton;
	private View.OnClickListener onClickListener;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PermissionTipViewPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setLayout();
	}

	public PermissionTipViewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setLayout();
	}

	public PermissionTipViewPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayout();
	}

	public PermissionTipViewPreference(Context context) {
		super(context);
		setLayout();
	}

	private void setLayout() {
		super.setLayoutResource(R.layout.forced_apps_permission);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		grantPermissionsButton = (Button) view.findViewById(R.id.forced_apps_permissions_enable);
		grantPermissionsButton.setOnClickListener(onClickListener);
	}

	public void setOnGrantPermissionClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		if (grantPermissionsButton != null)
			grantPermissionsButton.setOnClickListener(onClickListener);
	}

}
