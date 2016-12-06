package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;


public class CheckedInstalledApp extends LinearLayout implements Checkable {

	private ImageView icon;
	private TextView label;
	private TextView packageName;
	private CheckBox checkbox;

	private InstalledApp currentItem;

	public CheckedInstalledApp(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		icon = (ImageView) findViewById(R.id.forced_apps_item_icon);
		label = (TextView) findViewById(R.id.forced_apps_item_label);
		packageName = (TextView) findViewById(R.id.forced_apps_item_package);
		checkbox = (CheckBox)findViewById(R.id.forced_apps_item_checkbox);
	}

	@Override
	public void setChecked(boolean checked) {
		checkbox.setChecked(checked);
	}

	@Override
	public boolean isChecked() {
		return checkbox.isChecked();
	}

	@Override
	public void toggle() {
		checkbox.toggle();
	}

	public void setItem(InstalledApp item) {
		if (item != currentItem) {
			icon.setImageDrawable(item.icon);
			label.setText(item.label);
			packageName.setText(item.packageName);
			packageName.setVisibility(label.getText().equals(packageName.getText()) ? GONE : VISIBLE);

			currentItem = item;
		}
	}

}
