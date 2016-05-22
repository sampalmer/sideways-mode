package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.DiagnosticInfo;
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
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		showIcons(menu);
	}

	/**
	 * http://stackoverflow.com/a/22288914/238753
	 */
	private void showIcons(Menu menu) {
		if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
			try{
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, true);
			}
			catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
				Log.w(getClass().getSimpleName(), e);
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
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
					emailRecipients(),
					StringResource.format(R.string.email_feedback_subject, R.string.app_name, VersionInfo.packageInfo().versionName).toString(),
					""
				).send();
				break;
			case R.id.action_feedback_google_play:
				Intent intent = new Intent(Intent.ACTION_VIEW)
					.setData(Uri.parse("market://details?id=" + getActivity().getPackageName()));
				try {
					startActivity(intent);
				}
				catch (ActivityNotFoundException ex) {
					Toast.makeText(getActivity(), R.string.error_no_market_app, Toast.LENGTH_LONG).show();
				}

				break;
			case R.id.action_report:
				new Email(
					getActivity(),
					emailRecipients(),
					StringResource.format(R.string.email_problem_subject, R.string.app_name, VersionInfo.packageInfo().versionName).toString(),
					String.format("\n\nPlease describe the problem above (or at the bottom of the email)\n\nlogcat:\n%s\n\ndevice info:\n%s\n\nPlease describe the problem below:\n", DiagnosticInfo.logcat(), DiagnosticInfo.deviceInfo())
				).send();
				break;
			case R.id.action_uninstall:
				confirmUninstall();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private String emailRecipients() {
		return StringResource.get(R.string.app_name) + " <sampalmer4+androidautorotate@gmail.com>";
	}

	private void confirmUninstall() {
		final Uninstaller uninstaller = new Uninstaller(getActivity());

		uninstaller.uninstallOrPrompt();
	}

}
