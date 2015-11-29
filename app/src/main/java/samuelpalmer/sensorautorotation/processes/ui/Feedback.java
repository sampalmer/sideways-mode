package samuelpalmer.sensorautorotation.processes.ui;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;

public class Feedback {

	private final Email email;

	public Feedback() {
		email = new Email(
			new String[]{StringResource.get(R.string.app_name) + " <sampalmer4+androidautorotate@gmail.com>"},
			StringResource.format(R.string.feedback_email_subject, R.string.app_name, VersionInfo.packageInfo().versionName).toString(),
			"",
			null
		);
	}

	public void send() {
		email.send();
	}

}
