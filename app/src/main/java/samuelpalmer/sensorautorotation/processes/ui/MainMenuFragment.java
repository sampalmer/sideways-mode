package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;

public class MainMenuFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_instructions:
				startActivity(ComponentType.create(InstructionsActivity.class).intent());
				break;
			case R.id.action_feedback_email:
				new Email(
					getActivity(),
					ReportProblemActivity.emailRecipients(),
					StringResource.format(R.string.email_feedback_subject, R.string.app_name_full, VersionInfo.packageInfo().versionName).toString(),
					""
				).send();
				break;
			case R.id.action_feedback_google_play: {
				Intent intent = new Intent(Intent.ACTION_VIEW)
					.setData(Uri.parse("market://details?id=" + getActivity().getPackageName()));
				try {
					startActivity(intent);
				}
				catch (ActivityNotFoundException ex) {
					Toast.makeText(getActivity(), R.string.error_no_market_app, Toast.LENGTH_LONG).show();
				}

				break;
			}
			case R.id.action_report: {
				Intent intent = ComponentType.create(ReportProblemActivity.class)
						.intent()
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Don't want the user to lose what they've typed into the problem report when a different activity starts
				startActivity(intent);
				break;
			}
			case R.id.action_uninstall:
				confirmUninstall();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void confirmUninstall() {
		final Uninstaller uninstaller = new Uninstaller(getActivity());

		uninstaller.uninstallOrPrompt();
	}

}
