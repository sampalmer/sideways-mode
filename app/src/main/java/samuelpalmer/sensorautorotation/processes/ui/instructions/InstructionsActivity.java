package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;

public class InstructionsActivity extends UiActivity {
	
	public static final String EXTRA_FIRST_RUN = "first_run";
	
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
		
		setButtonDrawables(mNextButton, 0, R.drawable.navigation_next);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			}
		});
		
		setButtonDrawables(mPrevButton, R.drawable.navigation_previous, 0);
		mPrevButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() - 1);
			}
		});
		
		setButtonDrawables(mFinishButton, 0, R.drawable.navigation_finish);
		mFinishButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		updateBottomBar();
		
		setResult(RESULT_OK);
	}
	
	private void setButtonDrawables(Button button, @DrawableRes int start, @DrawableRes int end) {
		Drawable
			startDrawable = makeButtonDrawable(button, start),
			endDrawable = makeButtonDrawable(button, end);
		
		//The drawables must be set in code so the vector drawables work in old Android versions
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			button.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, endDrawable, null);
		else
			button.setCompoundDrawablesWithIntrinsicBounds(startDrawable, null, endDrawable, null);
	}
	
	/**
	 * Makes a drawable whose colour matches the button's text colour.
	 */
	private Drawable makeButtonDrawable(Button button, int drawableId) {
		if (drawableId == 0)
			return null;
		
		Drawable original = ContextCompat.getDrawable(this, drawableId);
		Drawable wrapped = DrawableCompat.wrap(original);
		DrawableCompat.setTintList(wrapped, button.getTextColors());
		return wrapped;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.instructions, menu);
		
		boolean isFirstRun = getIntent().getBooleanExtra(EXTRA_FIRST_RUN, false);
		if (isFirstRun) {
			ActionBar supportActionBar = getSupportActionBar();
			supportActionBar.setHomeButtonEnabled(false); // disable the button
			supportActionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
			supportActionBar.setDisplayShowHomeEnabled(false); // remove the icon
		}
		
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
