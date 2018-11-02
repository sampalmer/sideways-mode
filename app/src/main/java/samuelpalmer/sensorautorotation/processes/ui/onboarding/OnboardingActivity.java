package samuelpalmer.sensorautorotation.processes.ui.onboarding;

import android.os.Bundle;
import android.view.View;

import com.pixelcan.inkpageindicator.InkPageIndicator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class OnboardingActivity extends UiActivity {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.onboard_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        InkPageIndicator inkPageIndicator = findViewById(R.id.onboard_indicator);
        inkPageIndicator.setViewPager(viewPager);

        View getStartedButton = findViewById(R.id.onboard_get_started);
        getStartedButton.setOnClickListener(v -> {
            ApplicationSettings.getSharedPreferences(OnboardingActivity.this)
                .edit()
                .putBoolean(ApplicationSettings.KEY_USER_ONBOARDED, true)
                .apply();

            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();

            // TODO: Allow the user to press the back button to close out of the onboarding stuff
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return OnboardingPageFragment.newInstance(R.drawable.preference_incompatible_apps, "Your phone, your way", "Lying down, standing up, up to you.");
            else if (position == 1)
                return OnboardingPageFragment.newInstance(R.drawable.preference_apps, null, "No need to turn off auto-rotate when you lie on your side.");
            else if (position == 2)
                return OnboardingPageFragment.newInstance(R.drawable.menu_help, null, "Sync your favorite hydrocarbons and see them on any of your devices.");
            else
                throw new RuntimeException();
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
