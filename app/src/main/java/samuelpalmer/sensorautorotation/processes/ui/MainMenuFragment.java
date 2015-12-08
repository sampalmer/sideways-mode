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
import samuelpalmer.sensorautorotation.R;
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
				new Feedback().send();
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
