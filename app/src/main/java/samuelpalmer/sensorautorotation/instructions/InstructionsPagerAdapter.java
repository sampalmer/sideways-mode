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
	private static final int[] PAGE_TEXT_IDS = new int[] {
			R.string.instructions_introduction,
			R.string.instructions_modes,
			R.string.instructions_toggling,
			R.string.instructions_fixed_rotation,
			R.string.instructions_auto_rotation,
			R.string.instructions_overridden,
			R.string.instructions_uninstalling,
			R.string.instructions_help,
	};

	public InstructionsPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public int getCount() {
		return PAGE_TEXT_IDS.length;
	}

	@Override
	public Fragment getItem(final int position) {
		return new Fragment() {
			@Nullable
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				View page = inflater.inflate(R.layout.instructions_page, container, false);
				TextView pageTextView = (TextView) page.findViewById(R.id.instructions_page_text);
				pageTextView.setText(PAGE_TEXT_IDS[position]);

				return page;
			}
		};
	}
}
