package samuelpalmer.sensorautorotation.processes.ui;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.MultiProcessSharedPreferences;

/**
 * UI proxy to settings stored in background process.
 */
public class MainActivity extends UiActivity {

	private static final String TAG_MENU = "menu";
	private static final String TAG_FIRST_RUN_FRAGMENT = "first run";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		if (getFragmentManager().findFragmentByTag(TAG_MENU) == null)
			transaction.add(new MainMenuFragment(), TAG_MENU);
		if (getFragmentManager().findFragmentByTag(TAG_FIRST_RUN_FRAGMENT) == null)
			transaction.add(new FirstRunLauncherFragment(), TAG_FIRST_RUN_FRAGMENT);

		transaction.commit();
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		if (name.equals(ApplicationSettings.sharedPreferencesName()))
			return new MultiProcessSharedPreferences(name);
		else
			return super.getSharedPreferences(name, mode);
	}

}
