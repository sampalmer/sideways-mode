package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
			case R.id.action_feedback:
				new Feedback().send();
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
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.uninstall_title)
				.setIcon(R.drawable.ic_delete)
				.setMessage(R.string.uninstall_confirmation_message)
				.setPositiveButton(android.R.string.yes, new Uninstaller(getActivity()))
				.setNegativeButton(android.R.string.no, null)
				.create()
				.show();
	}

}
