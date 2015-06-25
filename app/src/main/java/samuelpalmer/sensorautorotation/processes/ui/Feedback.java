package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class Feedback {
	//The intent resolves to this when run on the official Android emulators if it doesn't match anything.
	private static final ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");

	private final Intent intent;

	public Feedback() {
		intent = new Intent(Intent.ACTION_SENDTO)
			.setData(new Uri.Builder().scheme("mailto").build())
			.putExtra(Intent.EXTRA_EMAIL, new String[]{"Sam Palmer <sampalmer4+androidautorotate@gmail.com>"})
			.putExtra(Intent.EXTRA_SUBJECT, StringResource.format(R.string.feedback_email_subject, R.string.app_name))
			.putExtra(Intent.EXTRA_TEXT, "")
		;
	}

	public void send() {
		if (!hasEmailApp() || !openEmail())
			Toaster.show(StringResource.get(R.string.error_no_email_app));
	}

	private boolean hasEmailApp() {
		ComponentName emailApp = intent.resolveActivity(Process.context().getPackageManager());
		return emailApp != null && !emailApp.equals(unsupportedAction);
	}

	private boolean openEmail() {
		try {
			// Needed to customise the chooser dialog title since it might default to "Share with"
			// Note that the chooser will still be skipped if only one app is matched
			Intent chooser = Intent.createChooser(intent, StringResource.get(R.string.email_app_chooser_title))
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //So the email activity doesn't take over the current one
			Process.context().startActivity(chooser);
			return true;
		}
		catch (ActivityNotFoundException ignored) {
			return false;
		}
	}
}
