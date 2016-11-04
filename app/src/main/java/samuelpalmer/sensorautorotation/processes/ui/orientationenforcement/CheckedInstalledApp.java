package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;


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
			new ImageLoader(item).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			label.setText(item.label);
			packageName.setText(item.packageName);
			packageName.setVisibility(label.getText().equals(packageName.getText()) ? GONE : VISIBLE);

			currentItem = item;
		}
	}

	/**
	 * Loads icons in the background to avoid lag when scrolling through the list
	 */
	class ImageLoader extends AsyncTask<Void, Void, Drawable> {

		private final InstalledApp installedApp;

		public ImageLoader(InstalledApp installedApp) {
			this.installedApp = installedApp;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//TODO: Hide imageview and show progress bar
			icon.setImageDrawable(null);
		}

		@Override
		protected Drawable doInBackground(Void... params) {
			return installedApp.loadIcon(SystemServices.packageManager());
		}

		@Override
		protected void onPostExecute(Drawable drawable) {
			super.onPostExecute(drawable);
			if (installedApp == currentItem) {
				//TODO: Show imageview and hide progress bar
				icon.setImageDrawable(drawable);
			}
		}

	}

}
