package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import samuelpalmer.common.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.Toaster;

class Email {
	
	private final Activity activity;
	private final Intent sendIntent;
	private final List<ResolveInfo> emailActivities;

	public Email(Activity activity, String recipient, String subject, CharSequence body) {
		this.activity = activity;

		//Putting the info in the URI seems to work better for the Sony Xperia M (Android 4.1.2) messaging app
		Uri uri = Uri.parse(
			String.format("mailto:%s?subject=%s",
				Uri.encode(recipient),
				Uri.encode(subject)
			)
		);

		sendIntent = new Intent(Intent.ACTION_SENDTO)
			.setData(uri)
			.putExtra(Intent.EXTRA_SUBJECT, subject) //Just in case the one in the query string is ignored
			.putExtra(Intent.EXTRA_TEXT, body); //We could also put this in the URI, but it could make the URI really long, which might cause problems

		emailActivities = activity.getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);

		//The official Android emulators seem to resolve a single "unsupported action" activity if the intent can't be handled.
		//Also, for some reason, I once saw the Android API 14 emulator resolve both the correct email app and the "unsupported action" activity.
		Iterator<ResolveInfo> iterator = emailActivities.iterator();
		while (iterator.hasNext()) {
			ResolveInfo resolveInfo = iterator.next();
			ActivityInfo firstActivity = resolveInfo.activityInfo;
			if (ComponentNameExtensions.makeFrom(firstActivity).equals(ComponentNameExtensions.unsupportedAction))
				iterator.remove();
		}
	}

	public boolean send() {
		boolean succeeded = hasEmailApp() && openEmail();
		if (!succeeded)
			Toaster.show(StringResource.get(R.string.email_error_no_email_app), Toast.LENGTH_SHORT);
		
		return succeeded;
	}

	private boolean hasEmailApp() {
		return !emailActivities.isEmpty();
	}

	private boolean openEmail() {

		List<Intent> emailAppIntents = new ArrayList<>(emailActivities.size());
		for (int i = 0; i < emailActivities.size(); ++i)
			emailAppIntents.add(
				new Intent(sendIntent)
					.setPackage(emailActivities.get(i).activityInfo.packageName)
			);

		try {
			// Needed to customise the chooser dialog title since it might default to "Share with"
			// Note that the chooser will still be skipped if only one app is matched
			Intent chooser = Intent.createChooser(emailAppIntents.remove(0), StringResource.get(R.string.email_app_chooser_title))
				.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailAppIntents.toArray(new Parcelable[emailAppIntents.size()]))
			;
			activity.startActivity(chooser);
			return true;
		}
		catch (ActivityNotFoundException ignored) {
			return false;
		}
	}

}
