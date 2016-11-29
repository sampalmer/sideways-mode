package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

//TODO: Keep the list of installed apps up-to-date (the user might install, uninstall, disable, or enable apps)
public class ForcedAppsChooserActivity extends UiActivity {

	private ProgressListView list;
	private LoadInstalledAppsTask loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forced_apps_chooser_activity);

		list = new ProgressListView();
		list.adapter = new InstalledAppAdapter(this);
		list.list = (ListView) findViewById(R.id.forced_apps_list);
		list.list.setAdapter(list.adapter);
		list.list.setItemsCanFocus(false); //Don't know if this is needed
		list.progressBar = (ProgressBar) findViewById(R.id.forced_apps_progress);

		loader = new LoadInstalledAppsTask(list);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		loader.cancel(true);
	}
}
