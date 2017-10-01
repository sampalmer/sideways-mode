package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.DiagnosticInfo;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.Toaster;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class EnterFeedbackActivity extends UiActivity {
	public static final String EXTRA_TYPE = "type";
	public static final String TYPE_FEEDBACK = "feedback";
	public static final String TYPE_PROBLEM = "problem";

	private final static String PROBLEM_LOGS_KEY = "logcat";
	private boolean isProblemReport;
	private String problemLogs;
	private EditText description;
	private Button send;

	private static String emailRecipients() {
		return CurrentProcess.context().getText(R.string.app_name) + " <sampalmer4+androidautorotate@gmail.com>";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_enter_feedback);

		description = findViewById(R.id.feedback_description);
		send = findViewById(R.id.feedback_send);
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

		// This activity saves the logs as soon as it's created.
		// This is to ensure the logs that get submitted reflect the state of things when the user first went to report a problem.
		if (isProblemReport) {
			if (savedInstanceState == null)
				problemLogs = DiagnosticInfo.logcat();
			else
				problemLogs = savedInstanceState.getString(PROBLEM_LOGS_KEY);

			description.setHint(R.string.report_problem_hint);
			setTitle(R.string.action_report);
		}
		else {
			description.setHint(R.string.enter_feedback_hint);
			setTitle(R.string.action_feedback);
			findViewById(R.id.feedback_disclosure).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (isProblemReport)
			outState.putString(PROBLEM_LOGS_KEY, problemLogs);
	}

	private void sendEmail() {
		String descriptionText = description.getText().toString();

		/*
		 * Problem reports don't require descriptions at this stage since sometimes the problem can be inferred from the logs anyway.
		 * Feedback reports do require descriptions since they're useless without them.
		 */
		description.setError(null);
		if (!isProblemReport && descriptionText.isEmpty()) {
			description.setError(CurrentProcess.context().getText(R.string.enter_feedback_nofeedback));
			return;
		}
		
		PackageInfo appInfo;
		try {
			appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		int formatStringResource = isProblemReport ? R.string.email_problem_subject : R.string.email_feedback_subject;
		String subject = CurrentProcess.context().getString(formatStringResource, CurrentProcess.context().getText(R.string.app_name), appInfo.versionName);
		
		String fullAppVersion = String.format(Locale.ENGLISH, "%s (%d)", appInfo.versionName, appInfo.versionCode);
		String body =
			isProblemReport
			?
			String.format("Problem:\n%s\n\nDevice info:\n%s\n\nApp version:\n%s\n\nLogs:\n%s\n\n", descriptionText, DiagnosticInfo.deviceInfo(), fullAppVersion, problemLogs)
			:
			descriptionText;
		
		Intent emailIntent = new Email(EnterFeedbackActivity.this, emailRecipients(), subject, body).getIntent();
		boolean intentStarted = false;
		
		if (emailIntent != null)
			try {
				startActivity(emailIntent);
				intentStarted = true;
			}
			catch (ActivityNotFoundException ignored) {}
		
		if (intentStarted) {
			//Only want to throw away the input if the user successfully got to the email client, but we can't know for sure.
			finish();
		}
		else
			Toaster.show(CurrentProcess.context().getText(R.string.email_error_no_email_app), Toast.LENGTH_SHORT);
	}
}
