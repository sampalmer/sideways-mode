package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;


public class PermissionTipViewPreference extends Preference {

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
		setWidgetLayoutResource(R.layout.forced_apps_permission_button);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View layout = super.onCreateView(parent);
		layout.setBackgroundColor(0xffe5e5e5);
		
		TextView title = (TextView) layout.findViewById(android.R.id.title);
		if (title != null) {
			title.setSingleLine(false);
			title.setMaxLines(Integer.MAX_VALUE);
		}
		
		return layout;
	}

}
