package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.utilities.Email;

public class ReportProblemActivity extends UiActivity {
	
	private final static String PROBLEM_LOGS_KEY = "logcat";
	
	private String problemLogs;
	private TextInputLayout descriptionLayout;
	private EditText description;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_report_problem);
		
		descriptionLayout = findViewById(R.id.problem_description_layout);
		description = findViewById(R.id.problem_description);

		// This activity saves the logs as soon as it's created.
		// This is to ensure the logs that get submitted reflect the state of things when the user first went to report a problem.
		if (savedInstanceState == null)
			problemLogs = DiagnosticInfo.logcat();
		else
			problemLogs = savedInstanceState.getString(PROBLEM_LOGS_KEY);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(PROBLEM_LOGS_KEY, problemLogs);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.report_problem, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_send) {
			sendEmail();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void sendEmail() {
		if (description.getText().length() < 5) {
			// Users have sent a lot of blank problem reports where the logs suggest nothing is wrong.
			// So we need the users to actually say what's wrong.
			descriptionLayout.setError(getText(R.string.report_problem_description_missing));
			return;
		}
		else
			descriptionLayout.setError(null);
		
		PackageInfo appInfo;
		try {
			appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String subject = getString(R.string.email_problem_subject, getText(R.string.app_name_full), appInfo.versionName);
		
		String fullAppVersion = String.format(Locale.ENGLISH, "%s (%d)", appInfo.versionName, appInfo.versionCode);
		String body = String.format(
			"App version:\n%s\n\nProblem:\n%s\n\nDevice info:\n%s\n\nSensors:\n%s\n\nLogs:\n%s\n\n",
			fullAppVersion,
			description.getText().toString(),
			DiagnosticInfo.deviceInfo(this),
			DiagnosticInfo.sensorDump(this),
			problemLogs
		);
		
		Intent emailIntent = new Email(
			ReportProblemActivity.this,
			getText(R.string.app_name_full) + " <sampalmer4+androidautorotate@gmail.com>",
			subject,
			body
		).getIntent(getText(R.string.email_app_chooser_title));
		
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
			Toast.makeText(getApplicationContext(), getResources().getText(R.string.email_error_no_email_app), Toast.LENGTH_SHORT).show();
	}
}
