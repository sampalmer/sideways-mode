package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import samuelpalmer.sensorautorotation.DiagnosticInfo;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;

public class ReportProblemActivity extends UiActivity {
	private final static String logcatKey = "logcat";
	private String logcat;
	private EditText description;
	private Button send;

	public static String emailRecipients() {
		return StringResource.get(R.string.app_name_full) + " <sampalmer4+androidautorotate@gmail.com>";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_report_problem);

		description = (EditText) findViewById(R.id.problem_description);
		send = (Button) findViewById(R.id.problem_send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		if (savedInstanceState == null)
			logcat = DiagnosticInfo.logcat();
		else
			logcat = savedInstanceState.getString(logcatKey);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(logcatKey, logcat);
	}

	private void sendEmail() {
		//No validation at this point, we'll see if users continue sending blank problem reports.

		new Email(
			ReportProblemActivity.this,
			emailRecipients(),
			StringResource.format(R.string.email_problem_subject, R.string.app_name_full, VersionInfo.packageInfo().versionName).toString(),
			String.format("Logcat:\n%s\n\nDevice info:\n%s\n\nProblem:\n%s\n", logcat, DiagnosticInfo.deviceInfo(), description.getText().toString())
		).send();

		finish();
	}
}
