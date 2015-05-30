package samuelpalmer.sensorautorotation.instructions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;

import samuelpalmer.sensorautorotation.R;

public class InstructionsActivity extends FragmentActivity {

	private ViewPager pager;
	private CirclePageIndicator indicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);

		pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new InstructionsPagerAdapter(InstructionsActivity.this.getSupportFragmentManager()));

		indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
	}

}
