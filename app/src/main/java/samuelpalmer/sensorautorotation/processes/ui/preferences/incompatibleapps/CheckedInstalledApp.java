package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import samuelpalmer.sensorautorotation.R;


public class CheckedInstalledApp extends LinearLayout {
	
	private PackageManager packageManager;
	
	private ImageView icon;
	private ProgressBar iconProgress;
	private TextView label;
	private TextView packageName;
	private CheckBox checkbox;

	private InstalledApp currentItem;
	private ImageLoader currentLoader;
	
	public CheckedInstalledApp(Context context) {
		super(context);
		init(context);
	}
	
	public CheckedInstalledApp(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CheckedInstalledApp(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	@SuppressWarnings("unused")
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public CheckedInstalledApp(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context) {
		packageManager = context.getPackageManager();
		
		LayoutInflater.from(context).inflate(R.layout.incompatible_apps_chooser_item, this);
		setOrientation(HORIZONTAL);
		setPadding(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
		setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_item_min_height));
		
		icon = findViewById(R.id.incompatible_apps_item_icon);
		iconProgress = findViewById(R.id.incompatible_item_icon_progress);
		label = findViewById(R.id.incompatible_apps_item_label);
		packageName = findViewById(R.id.incompatible_apps_item_package);
		checkbox = findViewById(R.id.incompatible_apps_item_checkbox);
	}
	
	public void setItem(InstalledApp item) {
		if (item != currentItem) {
			if (currentLoader != null)
				currentLoader.cancel(true);
			
			//Not using THREAD_POOL_EXECUTOR since it crashes if we load a lot of tasks in parallel
			currentLoader = new ImageLoader(item, new WeakReference<>(this));
			currentLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			
			label.setText(item.label);
			packageName.setText(item.packageName);
			packageName.setVisibility(label.getText().equals(packageName.getText()) ? GONE : VISIBLE);
		}
		
		// Even if we're changing to the same item, it's possible that the "isIncompatible" flag has since been changed.
		checkbox.setChecked(item.isIncompatible);
		currentItem = item;
	}
	
	/**
	 * Loads icons in the background to avoid lag when scrolling through the list
	 */
	private static class ImageLoader extends AsyncTask<Void, Void, Drawable> {
		
		private final InstalledApp installedApp;
		
		// Android Studio says this class could cause a memory leak by indirectly holding onto the context in a background thread.
		// So using a weak reference to get around that.
		private final WeakReference<CheckedInstalledApp> listItemReference;
		
		public ImageLoader(InstalledApp installedApp, WeakReference<CheckedInstalledApp> listItemReference) {
			this.installedApp = installedApp;
			this.listItemReference = listItemReference;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			CheckedInstalledApp listItem = listItemReference.get();
			if (listItem != null) {
				listItem.iconProgress.setVisibility(View.VISIBLE);
				listItem.icon.setVisibility(View.GONE);
			}
			else
				cancel(true);
		}
		
		@Override
		protected Drawable doInBackground(Void... params) {
			if (isCancelled())
				return null;
			
			CheckedInstalledApp listItem = listItemReference.get();
			if (listItem != null)
				return installedApp.loadIcon(listItem.packageManager);
			else {
				cancel(true);
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Drawable drawable) {
			super.onPostExecute(drawable);
			
			CheckedInstalledApp listItem = listItemReference.get();
			if (listItem != null) {
				// It's possible that this item has since been repurposed, which would make the loaded icon irrelevant
				if (installedApp == listItem.currentItem) {
					listItem.iconProgress.setVisibility(View.GONE);
					listItem.icon.setImageDrawable(drawable);
					listItem.icon.setVisibility(View.VISIBLE);
				}
			}
		}
		
	}

}
