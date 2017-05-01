package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

public class InstructionsPageFragment extends Fragment {
	
	public static InstructionsPageFragment newInstance(int pageIndex) {
		Bundle args = new Bundle();
		args.putInt(null, pageIndex);
		InstructionsPageFragment result = new InstructionsPageFragment();
		result.setArguments(args);
		return result;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_instructions_page, container, false);
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		TextView heading = (TextView) view.findViewById(R.id.instruction_heading);
		TextView contents = (TextView) view.findViewById(R.id.instruction_contents);
		
		int pageIndex = getArguments().getInt(null);
		InstructionsPage page = InstructionsPages.PAGES[pageIndex];
		
		heading.setText(page.heading);
		contents.setText(page.text());
	}
	
}
