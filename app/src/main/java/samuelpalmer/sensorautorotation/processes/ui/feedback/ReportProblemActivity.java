package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.FullScreenDialogActivity;
import samuelpalmer.sensorautorotation.utilities.Email;

public class ReportProblemActivity extends FullScreenDialogActivity {
	
	private final static String PROBLEM_LOGS_KEY = "logcat";
	
	private String problemLogs;
	private EditText steps;
	private EditText problem;
	private EditText expected;
	private EditText[] inputs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_report_problem);
		
		steps = findViewById(R.id.problem_steps);
		problem = findViewById(R.id.problem_problem);
		expected = findViewById(R.id.problem_expected);
		inputs = new EditText[] { steps, problem, expected };
		
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
		super.onCreateOptionsMenu(menu);
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
	
	@Override
	protected void onFullScreenDialogClosing() {
		boolean userHasEnteredText = false;
		
		for (EditText input : inputs) {
			String text = input.getText().toString().trim();
			
			if (!text.isEmpty()) {
				userHasEnteredText = true;
				break;
			}
		}
		
		if (userHasEnteredText) {
			new AlertDialog.Builder(this)
				.setMessage(R.string.report_problem_confirmation)
				.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {})
				.setPositiveButton(R.string.report_problem_discard, (dialogInterface, i) -> finish())
				.show();
		}
		else
			finish();
	}
	
	private void sendEmail() {
		if (!validateUserInput())
			return;
		
		PackageInfo appInfo;
		try {
			appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String subject = getString(R.string.email_problem_subject, getText(R.string.app_name_full), appInfo.versionName);
		
		String fullAppVersion = String.format(Locale.ENGLISH, "%s (%d)", appInfo.versionName, appInfo.versionCode);
		String body = String.format(
			"App version:\n%s\n\nWhat I did: %s\nWhat happened: %s\nWhat I expected: %s\n\nDevice info:\n%s\n\nSensors:\n%s\n\nLogs:\n%s\n\n",
			fullAppVersion,
			steps.getText().toString(),
			problem.getText().toString(),
			expected.getText().toString(),
			DiagnosticInfo.deviceInfo(this),
			DiagnosticInfo.sensorDump(this),
			problemLogs
		);
		
		Intent emailIntent = new Email(
			ReportProblemActivity.this,
			getText(R.string.app_name_full) + " <sampalmer4+androidautorotate@gmail.com>",
			subject,
			body
		).getIntent(getText(R.string.report_problem_send_with));
		
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
			Toast.makeText(getApplicationContext(), getResources().getText(R.string.report_problem_no_email_app), Toast.LENGTH_SHORT).show();
	}
	
	private boolean validateUserInput() {
		// Users have sent a lot of useless problem reports, so now they must fill out 3 mandatory fields.
		
		boolean isValid = true;
		
		for (EditText input : inputs) {
			String text = input.getText().toString().trim();

			// Not using material design errors here because they result in a lot of visual clutter.
			if (text.length() == 0) {
				input.setError(getText(R.string.report_problem_field_missing));
				isValid = false;
			}
			else if (text.length() < 5) {
				input.setError(getText(R.string.report_problem_field_too_short));
				isValid = false;
			}
			else
				input.setError(null);
		}
		
		return isValid;
	}
}
