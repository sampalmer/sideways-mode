package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.sensorautorotation.R;


public class InstructionsScreenFragment extends Fragment {

	public static final String TAG = "instructions_tag";

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.instructions_screen, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/**
		 * Manually adding fragment since adding it in XML:
		 * 1. Might be unsupported: http://stackoverflow.com/a/19815266/238753
		 * 2. Results in "java.lang.IllegalArgumentException: Binary XML file line #9: Duplicate id 0x7f0f008f, tag null, or parent id 0xffffffff with another fragment for samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsFragment"
		 */
		FragmentManager fragmentManager = getFragmentManager();
		Fragment existingFragment = fragmentManager.findFragmentByTag(TAG);

		FragmentTransaction ft = fragmentManager.beginTransaction();
		if (existingFragment != null)
			ft.remove(existingFragment);

		InstructionsFragment newFragment = new InstructionsFragment();
		ft.add(R.id.instructions_screen, newFragment, TAG);
		ft.commit();
	}
}
