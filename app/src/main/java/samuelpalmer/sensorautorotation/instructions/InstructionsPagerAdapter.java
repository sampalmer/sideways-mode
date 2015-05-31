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
import samuelpalmer.sensorautorotation.StringResource;

class InstructionsPagerAdapter extends FragmentPagerAdapter {
	private static final CharSequence[] PAGE_TEXT_IDS = new CharSequence[] {
			StringResource.format(R.string.instructions_introduction),
			StringResource.format(R.string.instructions_modes),
			StringResource.format(R.string.instructions_toggling, smallAppClause()),
			StringResource.format(R.string.instructions_fixed_rotation),
			StringResource.format(R.string.instructions_auto_rotation),
			StringResource.format(R.string.instructions_position),
			StringResource.format(R.string.instructions_position_changing),
			StringResource.format(R.string.instructions_overridden, R.string.notification_overridden),
			StringResource.format(R.string.instructions_uninstalling),
			StringResource.format(R.string.instructions_help)
	};

	private static String smallAppClause() {
		if (supportsSmallApp())
			return StringResource.format(R.string.instructions_toggling_small_app, R.string.app_name);
		else
			return "";
	}

	private static boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}

		return true;
	}

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
