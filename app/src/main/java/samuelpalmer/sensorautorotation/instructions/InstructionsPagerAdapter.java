package samuelpalmer.sensorautorotation.instructions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

class InstructionsPagerAdapter extends FragmentPagerAdapter {
	private static final InstructionsPage[] PAGES = new InstructionsPage[] {
			new InstructionsPage(R.string.instructions_introduction),
			new InstructionsPage(R.string.instructions_modes),
			new InstructionsPage(R.string.instructions_toggling, smallAppClause()),
			new InstructionsPage(R.string.instructions_fixed_rotation),
			new InstructionsPage(R.string.instructions_auto_rotation),
			new InstructionsPage(R.string.instructions_position),
			new InstructionsPage(R.string.instructions_position_changing),
			new InstructionsPage(R.string.instructions_overridden, R.string.notification_overridden),
			new InstructionsPage(R.string.instructions_uninstalling),
			new InstructionsPage(R.string.instructions_help) {
				@Override
				public void applyText(TextView pageText) {
					super.applyText(pageText);
					pageText.setGravity(Gravity.START);
				}
			}
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
		return PAGES.length;
	}

	@Override
	public Fragment getItem(final int position) {
		return new Fragment() {
			@Nullable
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				View page = inflater.inflate(R.layout.instructions_page, container, false);
				TextView pageTextView = (TextView) page.findViewById(R.id.instructions_page_text);
				PAGES[position].applyText(pageTextView);
				return page;
			}
		};
	}
}
