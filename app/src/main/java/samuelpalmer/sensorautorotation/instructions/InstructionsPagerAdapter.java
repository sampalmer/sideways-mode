package samuelpalmer.sensorautorotation.instructions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

class InstructionsPagerAdapter extends FragmentPagerAdapter {

	private final InstructionsPages pages;

	public InstructionsPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
		this.pages = new InstructionsPages();
	}

	@Override
	public int getCount() {
		return pages.PAGES.length;
	}

	@Override
	public Fragment getItem(final int position) {
		return new Fragment() {
			@Nullable
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				View pageLayout = inflater.inflate(R.layout.instructions_page, container, false);
				InstructionsPage page = pages.PAGES[position];

				TextView pageTextView = (TextView) pageLayout.findViewById(R.id.instructions_page_text);
				page.applyText(pageTextView);

				com.caverock.androidsvg.SVGImageView background = (com.caverock.androidsvg.SVGImageView) pageLayout.findViewById(R.id.instructions_page_background);
				background.setAlpha(0.1f);
				page.setBackground(background);

				return pageLayout;
			}
		};
	}
}
