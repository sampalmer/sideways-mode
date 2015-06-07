package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class ShortcutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent shortcut = new Intent();

		//If you try to send a broadcast from a shortcut, Android seems to just say "App not installed", so we're using a dummy activity.
		Intent launchIntent = new Intent(this, ToggleRotationLockActivity.class);

		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, StringResource.get(R.string.toggle_rotation_lock));
		shortcut.putExtra(
				Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));

		setResult(RESULT_OK, shortcut);
		finish();
	}
}
