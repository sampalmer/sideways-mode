package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;


public class CheckedInstalledApp extends LinearLayout implements Checkable {
	
	private final PackageManager packageManager;
	
	private ImageView icon;
	private ProgressBar iconProgress;
	private TextView label;
	private TextView packageName;
	private CheckBox checkbox;

	private InstalledApp currentItem;
	private ImageLoader currentLoader;
	
	public CheckedInstalledApp(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		packageManager = context.getPackageManager();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		icon = (ImageView) findViewById(R.id.forced_apps_item_icon);
		iconProgress = (ProgressBar) findViewById(R.id.forced_apps_item_icon_progress);
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
			if (currentLoader != null)
				currentLoader.cancel(true);
			
			//Not using THREAD_POOL_EXECUTOR since it crashes if we load a lot of tasks in parallel
			currentLoader = new ImageLoader(item);
			currentLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			
			label.setText(item.label);
			packageName.setText(item.packageName);
			packageName.setVisibility(label.getText().equals(packageName.getText()) ? GONE : VISIBLE);

			currentItem = item;
		}
	}
	
	/**
	 * Loads icons in the background to avoid lag when scrolling through the list
	 */
	private class ImageLoader extends AsyncTask<Void, Void, Drawable> {
		
		private final InstalledApp installedApp;
		
		public ImageLoader(InstalledApp installedApp) {
			this.installedApp = installedApp;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			iconProgress.setVisibility(View.VISIBLE);
			icon.setVisibility(View.GONE);
		}
		
		@Override
		protected Drawable doInBackground(Void... params) {
			if (isCancelled())
				return null;
			
			return installedApp.loadIcon(packageManager);
		}
		
		@Override
		protected void onPostExecute(Drawable drawable) {
			super.onPostExecute(drawable);
			
			if (installedApp == currentItem) {
				iconProgress.setVisibility(View.GONE);
				icon.setImageDrawable(drawable);
				icon.setVisibility(View.VISIBLE);
			}
		}
		
	}

}
