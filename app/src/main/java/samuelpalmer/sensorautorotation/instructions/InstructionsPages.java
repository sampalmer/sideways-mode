package samuelpalmer.sensorautorotation.instructions;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

class InstructionsPages {
	public final InstructionsPage[] PAGES = new InstructionsPage[] {
			new InstructionsPage(R.string.instructions_introduction_heading, R.string.instructions_introduction_contents, R.string.app_name),
			new InstructionsPage(R.string.instructions_modes_heading, R.string.instructions_modes_contents, R.string.screen_rotation_enabled, R.string.screen_rotation_disabled_portrait),
			new InstructionsPage(R.string.instructions_toggling_heading, R.string.instructions_toggling_contents, smallAppClause()),
			new InstructionsPage(R.string.instructions_fixed_rotation_heading, R.string.instructions_fixed_rotation_contents),
			new InstructionsPage(R.string.instructions_auto_rotation_heading, R.string.instructions_auto_rotation_contents, R.string.screen_rotation_enabled, R.string.app_name),
			new InstructionsPage(R.string.instructions_position_heading, R.string.instructions_position_contents),
			new InstructionsPage(R.string.instructions_position_changing_heading, R.string.instructions_position_changing_contents, R.string.app_name),
			new InstructionsPage(R.string.instructions_overridden_heading, R.string.instructions_overridden_contents, R.string.app_name, R.string.notification_overridden),
			new InstructionsPage(R.string.instructions_uninstalling_heading, R.string.instructions_uninstalling_contents, R.string.action_uninstall, R.string.app_name),
			new InstructionsPage(R.string.instructions_help_heading, R.string.instructions_help_contents, R.string.screen_rotation_enabled)
	};

	private CharSequence smallAppClause() {
		if (supportsSmallApp())
			return StringResource.format(R.string.instructions_toggling_small_app, R.string.app_name);
		else
			return "";
	}

	private boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}

		return true;
	}
}
