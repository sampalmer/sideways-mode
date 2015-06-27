package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_instructions:
				startActivity(ComponentType.create(InstructionsActivity.class).makeIntent());
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
				.setIcon(android.R.drawable.ic_menu_delete)
				.setMessage(R.string.uninstall_confirmation_message)
				.setPositiveButton(android.R.string.yes, new Uninstaller(getActivity()))
				.setNegativeButton(android.R.string.no, null)
				.create()
				.show();
	}

}
