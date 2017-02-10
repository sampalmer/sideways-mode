package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.sensorautorotation.R;


public class InstructionsScreenFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.instructions_screen, container, false);
	}

}
