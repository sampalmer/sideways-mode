package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;


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
	
	private TextView disabledText;
	private Button disabledViewError;
	
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.dashboard_disabled_contents, this, true);
		
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_HORIZONTAL);
		
		disabledText = (TextView) findViewById(R.id.dashboard_disabled_text);
		disabledViewError = (Button) findViewById(R.id.dashboard_disabled_view_error);
	}
	
	public void setLastError(final EnvironmentError lastError) {
		/*
		 * Not showing any error details so the user has to view the full error info and actions.
		 * This is to reduce the chance of the user blaming the developer for the error and sending useless error reports.
		 */
		disabledText.setText(StringResource.format(lastError == null ? R.string.app_disabled_message : R.string.app_disabled_because_of_error, R.string.app_name_full));
		
		if (lastError == null) {
			disabledViewError.setVisibility(View.GONE);
			disabledViewError.setOnClickListener(null);
		}
		else {
			disabledViewError.setVisibility(View.VISIBLE);
			disabledViewError.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						lastError.showError().send();
					} catch (PendingIntent.CanceledException e) {
						throw new RuntimeException(e);
					}
					
					if (DashboardDialogCoordinator.dialogExists())
						DashboardDialogCoordinator.dismissDialog();
				}
			});
		}
	}
	
}
