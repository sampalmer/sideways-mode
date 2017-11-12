package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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
	private EditText steps;
	private EditText problem;
	private EditText expected;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_report_problem);
		
		steps = findViewById(R.id.problem_steps);
		problem = findViewById(R.id.problem_problem);
		expected = findViewById(R.id.problem_expected);

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
		
		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		supportActionBar.setHomeAsUpIndicator(R.drawable.close);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_send:
				sendEmail();
				return true;
			case android.R.id.home: // User pressed action bar back/close button
				finish();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
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
	
	private boolean validateUserInput() {
		// Users have sent a lot of useless problem reports, so now they must fill out 3 mandatory fields.
		
		EditText[] inputs = new EditText[] { steps, problem, expected };
		
		boolean isValid = true;
		
		for (EditText input : inputs) {
			if (input.getText().toString().trim().length() < 5) {
				// Not using material design errors here because they result in a lot of visual clutter.
				input.setError(getText(R.string.report_problem_field_missing));
				isValid = false;
			}
			else
				steps.setError(null);
		}
		
		return isValid;
	}
}
