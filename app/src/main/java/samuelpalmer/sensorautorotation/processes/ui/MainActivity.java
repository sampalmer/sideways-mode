package samuelpalmer.sensorautorotation.processes.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;

public class MainActivity extends PreferencesActivity {

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

}
