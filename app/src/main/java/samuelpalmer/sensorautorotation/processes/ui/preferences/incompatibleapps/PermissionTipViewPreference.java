package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
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
        
        holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tip_view_background));
        
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(Integer.MAX_VALUE);
        }
    }
    
}
