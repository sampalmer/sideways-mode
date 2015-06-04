package samuelpalmer.sensorautorotation.instructions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

public class InstructionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);

		LinearLayout container = (LinearLayout) findViewById(R.id.container);

		InstructionsPage[] pages = new InstructionsPages().PAGES;
		for (InstructionsPage page : pages)
			container.addView(inflate(page));
	}

	private View inflate(InstructionsPage page) {
		@SuppressLint("InflateParams") //The ids get mixed up if we add it to the container straight away
		View pageLayout = getLayoutInflater().inflate(R.layout.instructions_page, null);

		TextView heading = (TextView) pageLayout.findViewById(R.id.instruction_heading);
		TextView contents = (TextView) pageLayout.findViewById(R.id.instruction_contents);

		heading.setText(page.heading);
		contents.setText(page.text());
		return pageLayout;
	}

}
