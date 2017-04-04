package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

public class InstructionsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_instructions, container, false);

		LinearLayout pageContainer = (LinearLayout) result.findViewById(R.id.container);

		InstructionsPage[] pages = new InstructionsPages().PAGES;
		for (InstructionsPage page : pages)
			pageContainer.addView(inflate(inflater, page));

		return result;
	}

	private View inflate(LayoutInflater inflater, InstructionsPage page) {
		@SuppressLint("InflateParams")
		//The ids get mixed up if we add it to the container straight away
		View pageLayout = inflater.inflate(R.layout.instructions_page, null);

		TextView heading = (TextView) pageLayout.findViewById(R.id.instruction_heading);
		TextView contents = (TextView) pageLayout.findViewById(R.id.instruction_contents);

		heading.setText(page.heading);
		contents.setText(page.text());
		return pageLayout;
	}

}
