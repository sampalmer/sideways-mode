package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class InstructionsActivity extends UiActivity {
	
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private Button mNextButton;
	private Button mPrevButton;
	private Button mFinishButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		
		mNextButton = (Button) findViewById(R.id.next_button);
		mPrevButton = (Button) findViewById(R.id.prev_button);
		mFinishButton = (Button) findViewById(R.id.finish_button);
		
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			
			@Override
			public void onPageSelected(int position) {
				updateBottomBar();
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		
		copyTextColourToDrawables(mNextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			}
		});
		
		copyTextColourToDrawables(mPrevButton);
		mPrevButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() - 1);
			}
		});
		
		copyTextColourToDrawables(mFinishButton);
		mFinishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		updateBottomBar();
		
		setResult(RESULT_OK);
	}
	
	private static void copyTextColourToDrawables(Button button) {
		Drawable[] drawables = button.getCompoundDrawables();
		for (int i = 0; i < drawables.length; ++i) {
			Drawable original = drawables[i];
			if (original != null) {
				Drawable wrapped = DrawableCompat.wrap(original);
				DrawableCompat.setTintList(wrapped, button.getTextColors());
				drawables[i] = wrapped;
			}
		}
		button.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_instructions, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.skip) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0)
			super.onBackPressed();
		else
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
	}
	
	private void updateBottomBar() {
		if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
			mNextButton.setVisibility(View.INVISIBLE);
			mFinishButton.setVisibility(View.VISIBLE);
		} else {
			mNextButton.setVisibility(View.VISIBLE);
			mFinishButton.setVisibility(View.INVISIBLE);
		}
		
		mPrevButton.setVisibility(mPager.getCurrentItem() == 0 ? View.INVISIBLE : View.VISIBLE);
	}
	
	private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			return InstructionsPageFragment.newInstance(position);
		}
		
		@Override
		public int getCount() {
			return InstructionsPages.PAGES.length;
		}
	}

}
