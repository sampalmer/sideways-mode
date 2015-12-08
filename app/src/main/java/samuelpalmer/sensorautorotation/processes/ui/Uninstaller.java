package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

class Uninstaller {

	private final Activity parent;
	private final ServiceSynchroniser serviceSynchroniser;

	public Uninstaller(Activity parent) {
		this.parent = parent;
		serviceSynchroniser = new ServiceSynchroniser(ApplicationSettings.remote());
	}

	void uninstallOrPrompt() {
		if (serviceSynchroniser.serviceEnabled())
			new AlertDialog.Builder(parent)
				.setTitle(R.string.uninstall_title)
				.setIcon(R.drawable.delete)
				.setMessage(R.string.uninstall_confirmation_message)
				.setPositiveButton(android.R.string.yes, onClickListener)
				.setNegativeButton(android.R.string.no, null)
				.create()
				.show();
		else
			uninstall();
	}

	private final OnClickListener onClickListener = new OnClickListener() {
		@Override

		public void onClick(DialogInterface dialog, int id) {
			uninstall();
		}

	};

	private void uninstall() {
		serviceSynchroniser.disableService();
		parent.startActivity(uninstallIntent());
	}

	private Intent uninstallIntent() {
		return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri());
	}

	private Uri packageUri() {
		return Uri.fromParts("package", parent.getPackageName(), null);
	}
}
