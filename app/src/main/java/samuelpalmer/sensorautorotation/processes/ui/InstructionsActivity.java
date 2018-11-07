package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.feedback.ReportProblemActivity;

public class InstructionsActivity extends FullScreenDialogActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		TextView uninstallingContents = findViewById(R.id.instructions_uninstalling_contents);
		uninstallingContents.setText(
			getResources().getString(
				R.string.instructions_uninstalling_contents,
				getResources().getText(R.string.app_name_full)
			)
		);
		
		TextView incompatibleAppsContents = findViewById(R.id.instructions_incompatible_apps_contents);
		incompatibleAppsContents.setText(
			getResources().getString(
				R.string.instructions_incompatible_apps_contents,
				getResources().getText(R.string.app_name_full),
				getResources().getText(R.string.preference_incompatible_apps_title)
			)
		);
		
		Button reportProblem = findViewById(R.id.instructions_report_problem);
		reportProblem.setOnClickListener(v -> {
			Intent intent = new Intent(InstructionsActivity.this, ReportProblemActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
			startActivity(intent);
			
			finish(); // This activity and the problem report one are both full screen dialogs. So we'll finish this one so we don't end up with a dialog on top of another dialog.
		});
	}
	
}
