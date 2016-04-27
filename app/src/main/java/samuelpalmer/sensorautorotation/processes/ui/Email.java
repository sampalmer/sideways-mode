package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class Email {

	//The intent resolves to this when run on the official Android emulators if it doesn't match anything.
	private static final ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
	private final Intent sendIntent;
	private final List<ResolveInfo> emailActivities;

	public Email(String recipient, String subject, CharSequence body) {
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

		emailActivities = Process.context().getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);

		//The official Android emulators seem to resolve a single "unsupported action" activity if the intent can't be handled.
		//Also, for some reason, I once saw the Android API 14 emulator resolve both the correct email app and the "unsupported action" activity.
		Iterator<ResolveInfo> iterator = emailActivities.iterator();
		while (iterator.hasNext()) {
			ResolveInfo resolveInfo = iterator.next();
			ActivityInfo firstActivity = resolveInfo.activityInfo;
			if (makeComponentName(firstActivity).equals(unsupportedAction))
				iterator.remove();
		}
	}

	public void send() {
		if (!hasEmailApp() || !openEmail())
			Toaster.show(StringResource.get(R.string.error_no_email_app));
	}

	private boolean hasEmailApp() {
		return emailActivities.size() > 0;
	}

	private static ComponentName makeComponentName(ActivityInfo activity) {
		boolean isRelativeClassName = activity.name.length() > 0 && activity.name.startsWith(".");
		String fullClassName = isRelativeClassName ? activity.packageName + activity.name : activity.name;
		return new ComponentName(activity.packageName, fullClassName);
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
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //So the email activity doesn't take over the current one
				.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailAppIntents.toArray(new Parcelable[emailAppIntents.size()]))
			;
			Process.context().startActivity(chooser);
			return true;
		}
		catch (ActivityNotFoundException ignored) {
			return false;
		}
	}

}
