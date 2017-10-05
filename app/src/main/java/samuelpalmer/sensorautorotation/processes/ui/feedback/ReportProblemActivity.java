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
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.Toaster;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class ReportProblemActivity extends UiActivity {
	
	private final static String PROBLEM_LOGS_KEY = "logcat";
	
	private String problemLogs;
	private EditText description;
	private Button send;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_report_problem);

		description = findViewById(R.id.problem_description);
		send = findViewById(R.id.problem_send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		// This activity saves the logs as soon as it's created.
		// This is to ensure the logs that get submitted reflect the state of things when the user first went to report a problem.
		if (savedInstanceState == null)
			problemLogs = DiagnosticInfo.logcat();
		else
			problemLogs = savedInstanceState.getString(PROBLEM_LOGS_KEY);
		
		setTitle(R.string.action_report);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(PROBLEM_LOGS_KEY, problemLogs);
	}

	private void sendEmail() {
		/*
		 * Problem reports don't require descriptions at this stage since sometimes the problem can be inferred from the logs anyway.
		 */
		description.setError(null);
		
		PackageInfo appInfo;
		try {
			appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String subject = CurrentProcess.context().getString(R.string.email_problem_subject, CurrentProcess.context().getText(R.string.app_name), appInfo.versionName);
		
		String fullAppVersion = String.format(Locale.ENGLISH, "%s (%d)", appInfo.versionName, appInfo.versionCode);
		String body = String.format(
			"Problem:\n%s\n\nDevice info:\n%s\n\nApp version:\n%s\n\nLogs:\n%s\n\n",
			description.getText().toString(),
			DiagnosticInfo.deviceInfo(),
			fullAppVersion,
			problemLogs
		);
		
		Intent emailIntent = new Email(
			ReportProblemActivity.this,
			CurrentProcess.context().getText(R.string.app_name) + " <sampalmer4+androidautorotate@gmail.com>",
			subject,
			body
		).getIntent();
		
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
