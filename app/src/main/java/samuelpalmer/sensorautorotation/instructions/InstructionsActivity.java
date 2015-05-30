package samuelpalmer.sensorautorotation.instructions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.viewpagerindicator.CirclePageIndicator;

import samuelpalmer.sensorautorotation.R;

public class InstructionsActivity extends FragmentActivity {

	private ViewPager pager;
	private CirclePageIndicator indicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);

		//Enables "up" navigation in API 14 & 15
		getActionBar().setDisplayHomeAsUpEnabled(true);

		pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new InstructionsPagerAdapter(InstructionsActivity.this.getSupportFragmentManager()));

		indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			//Enables "up" navigation in API 14 & 15
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
