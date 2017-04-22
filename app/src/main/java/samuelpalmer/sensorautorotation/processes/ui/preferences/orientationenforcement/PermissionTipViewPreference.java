package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		LayoutInflater layoutInflater =	(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.forced_apps_permission, parent, false);
		View contents = super.onCreateView(container);
		container.addView(contents);

		TextView title = (TextView) contents.findViewById(android.R.id.title);
		if (title != null) {
			title.setSingleLine(false);
			title.setMaxLines(Integer.MAX_VALUE);
		}

		return container;
	}

}
