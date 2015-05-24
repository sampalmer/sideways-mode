package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

class Uninstaller implements DialogInterface.OnClickListener {

	private final Activity parent;

	public Uninstaller(Activity parent) {
		this.parent = parent;
	}

	public void onClick(DialogInterface dialog, int id) {
		uninstall();
	}

	private void uninstall() {
		disableService();
		parent.startActivity(uninstallIntent());
	}

	private void disableService() {
		new ApplicationSettings().enabled().setAndCommit(false);
	}

	private Intent uninstallIntent() {
		return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri());
	}

	private Uri packageUri() {
		return Uri.fromParts("package", parent.getPackageName(), null);
	}

}
