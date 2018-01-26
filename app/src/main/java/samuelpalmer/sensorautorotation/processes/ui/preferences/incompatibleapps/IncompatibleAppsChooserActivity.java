package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.os.AsyncTask;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

//TODO: Keep the list of installed apps up-to-date (the user might install, uninstall, disable, or enable apps)
public class IncompatibleAppsChooserActivity extends UiActivity {

	private ProgressListView list;
	private LoadInstalledAppsTask loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incompatible_apps_chooser);

		list = new ProgressListView();
		list.adapter = new InstalledAppAdapter(this);
		list.list = findViewById(R.id.incompatible_apps_list);
		list.list.setAdapter(list.adapter);
		list.list.setItemsCanFocus(false); //Don't know if this is needed
		list.progressBar = findViewById(R.id.incompatible_apps_progress);

		loader = new LoadInstalledAppsTask(this, list);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loader.cancel(true);
	}
}
