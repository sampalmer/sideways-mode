package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;

public class ShortcutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent shortcut = new Intent();
		Intent launchIntent = new Intent(this, ToggleRotationLockActivity.class);

		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.toggle_rotation_lock));
		shortcut.putExtra(
				Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_screen_rotation_enabled));

		setResult(RESULT_OK, shortcut);
		finish();
	}
}
