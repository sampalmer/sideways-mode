package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;

public class InstructionsPageFragment extends Fragment {
	
	private static final String KEY_HEADING = "heading";
	private static final String KEY_TEXT = "text";
	private static final String KEY_ARGUMENTS = "textArguments";
	
	public static InstructionsPageFragment newInstance(@StringRes int heading, @StringRes int textId, @StringRes int... textArguments) {
		Bundle args = new Bundle();
		args.putInt(KEY_HEADING, heading);
		args.putInt(KEY_TEXT, textId);
		args.putIntArray(KEY_ARGUMENTS, textArguments);
		
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
		
		//Args
		Bundle args = getArguments();
		int headingId = args.getInt(KEY_HEADING);
		int textId = args.getInt(KEY_TEXT);
		
		int[] textArgumentIds = args.getIntArray(KEY_ARGUMENTS);
		assert textArgumentIds != null;
		Object[] textArgumentObjects = new CharSequence[textArgumentIds.length];
		for (int i = 0; i < textArgumentIds.length; ++i)
			textArgumentObjects[i] = CurrentProcess.context().getText(textArgumentIds[i]);
		
		CharSequence contentsText = CurrentProcess.context().getString(textId, textArgumentObjects);
		
		//View stuff
		TextView heading = view.findViewById(R.id.instruction_heading);
		TextView contents = view.findViewById(R.id.instruction_contents);
		
		heading.setText(headingId);
		contents.setText(contentsText);
	}
	
}
