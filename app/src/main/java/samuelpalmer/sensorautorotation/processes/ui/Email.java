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
import java.util.List;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class Email {

	//The intent resolves to this when run on the official Android emulators if it doesn't match anything.
	private static final ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
	private final Intent sendIntent;
	private final List<ResolveInfo> emailActivities;

	public Email(String[] recipients, String subject, CharSequence body, Attachment attachment) {
		if (attachment != null) {
			sendIntent = new Intent(Intent.ACTION_SEND);
			attachment.addTo(sendIntent);
		}
		else
			sendIntent = emailIntent();

		sendIntent
			.putExtra(Intent.EXTRA_EMAIL, recipients)
			.putExtra(Intent.EXTRA_SUBJECT, subject)
			.putExtra(Intent.EXTRA_TEXT, body);

		emailActivities = Process.context().getPackageManager().queryIntentActivities(emailIntent(), PackageManager.MATCH_DEFAULT_ONLY);
	}

	private static Intent emailIntent() {
		return new Intent(Intent.ACTION_SENDTO)
			.setData(new Uri.Builder().scheme("mailto").build());
	}

	public void send() {
		if (!hasEmailApp() || !openEmail())
			Toaster.show(StringResource.get(R.string.error_no_email_app));
	}

	private boolean hasEmailApp() {
		if (emailActivities.size() > 0) {
			ActivityInfo firstActivity = emailActivities.get(0).activityInfo;
			return !makeComponentName(firstActivity).equals(unsupportedAction);
		}

		return false;
	}

	private static ComponentName makeComponentName(ActivityInfo firstActivity) {
		boolean isRelativeClassName = firstActivity.name.length() > 0 && firstActivity.name.startsWith(".");
		String fullClassName = isRelativeClassName ? firstActivity.packageName + firstActivity.name : firstActivity.name;
		return new ComponentName(firstActivity.packageName, fullClassName);
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

	public static class Attachment {

		private final Uri uri;
		private final String mimeType;

		/**
		 * @param uri
		 *
		 * Must be accessible by the email application.
		 *
		 * For file URIs, ensure the file is world-readable.
		 * Setting the world-readable flag is apparently deprecated,
		 * so it might be better to put the file in the user's downloads directory.
		 * However, putting the user's info in a easily-accessed file might raise security concerns.
		 * Putting the file in the user's downloads could waste space if the file is full of logs.
		 *
		 * Sounds like the content-provider mechanism combined with FLAG_GRANT_READ_URI_PERMISSION
		 * might be the way to go. Or just use the email body instead of an attachment.
		 */
		public Attachment(Uri uri, String mimeType) {
			this.uri = uri;
			this.mimeType = mimeType;
		}

		void addTo(Intent intent) {
			intent
				.setType(mimeType)
				.putExtra(Intent.EXTRA_STREAM, uri);
		}

	}

}
