package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import samuelpalmer.sensorautorotation.DiagnosticInfo;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class EnterFeedbackActivity extends UiActivity {
	public static final String EXTRA_TYPE = "type";
	public static final String TYPE_FEEDBACK = "feedback";
	public static final String TYPE_PROBLEM = "problem";

	private final static String problemLogsKey = "logcat";
	private boolean isProblemReport;
	private String problemLogs;
	private EditText description;
	private Button send;

	private static String emailRecipients() {
		return StringResource.get(R.string.app_name_full) + " <sampalmer4+androidautorotate@gmail.com>";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_enter_feedback);

		description = (EditText) findViewById(R.id.feedback_description);
		send = (Button) findViewById(R.id.feedback_send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		String type = getIntent().getStringExtra(EXTRA_TYPE);
		if (type == null)
			throw new RuntimeException("Feedback type not specified");

		switch (type) {
			case TYPE_PROBLEM:
				isProblemReport = true;
				break;
			case TYPE_FEEDBACK:
				isProblemReport = false;
				break;
			default:
				throw new RuntimeException("Unknown feedback type: " + type);
		}


		if (isProblemReport) {
			if (savedInstanceState == null)
				problemLogs = DiagnosticInfo.logcat();
			else
				problemLogs = savedInstanceState.getString(problemLogsKey);

			description.setHint(R.string.report_problem_hint);
			setTitle(R.string.action_report);
		}
		else {
			description.setHint(R.string.enter_feedback_hint);
			setTitle(R.string.action_feedback);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (isProblemReport)
			outState.putString(problemLogsKey, problemLogs);
	}

	private void sendEmail() {
		String descriptionText = description.getText().toString();

		/*
		 * Problem reports don't require descriptions at this stage since sometimes the problem can be inferred from the logs anyway.
		 * Feedback reports do require descriptions since they're useless without them.
		 */
		description.setError(null);
		if (!isProblemReport && descriptionText.isEmpty()) {
			description.setError(StringResource.get(R.string.enter_feedback_nofeedback));
			return;
		}

		String subject = StringResource.format(
			isProblemReport ? R.string.email_problem_subject : R.string.email_feedback_subject,
			R.string.app_name_full,
			VersionInfo.packageInfo().versionName
		).toString();

		String body =
			isProblemReport
			?
			String.format("Logcat:\n%s\n\nDevice info:\n%s\n\nProblem:\n%s\n", problemLogs, DiagnosticInfo.deviceInfo(), descriptionText)
			:
			descriptionText;

		new Email(EnterFeedbackActivity.this, emailRecipients(), subject,body).send();

		finish();
	}
}
