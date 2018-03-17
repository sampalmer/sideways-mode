package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;


public class PermissionTipViewPreference extends Preference {
	
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
		setWidgetLayoutResource(R.layout.incompatible_apps_permission_button);
	}
	
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		
		holder.itemView.setBackgroundColor(0xffe5e5e5);
		
		TextView title = (TextView) holder.findViewById(android.R.id.title);
		if (title != null) {
			title.setSingleLine(false);
			title.setMaxLines(Integer.MAX_VALUE);
		}
	}
	
}
