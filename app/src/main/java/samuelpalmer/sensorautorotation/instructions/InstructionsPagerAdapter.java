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
			new InstructionsPage("rotation.svg", R.string.instructions_introduction, R.string.app_name),
			new InstructionsPage("notification.svg", R.string.instructions_modes, R.string.screen_rotation_enabled, R.string.screen_rotation_disabled_portrait),
			new InstructionsPage("rotation.svg", R.string.instructions_toggling, smallAppClause()),
			new InstructionsPage("portrait_lock.svg", R.string.instructions_fixed_rotation),
			new InstructionsPage("rotation.svg", R.string.instructions_auto_rotation, R.string.screen_rotation_enabled),
			new InstructionsPage("person.svg", R.string.instructions_position),
			new InstructionsPage("person.svg", R.string.instructions_position_changing),
			new InstructionsPage("rotation_overridden.svg", R.string.instructions_overridden, R.string.notification_overridden),
			new InstructionsPage("delete.svg", R.string.instructions_uninstalling, R.string.action_uninstall),
			new InstructionsPage("need_help.svg", R.string.instructions_help, R.string.screen_rotation_enabled) {
				@Override
				public void applyText(TextView pageText) {
					super.applyText(pageText);
					pageText.setGravity(Gravity.START);
				}
			}
	};

	private static CharSequence smallAppClause() {
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
				View pageLayout = inflater.inflate(R.layout.instructions_page, container, false);
				InstructionsPage page = PAGES[position];

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
