package samuelpalmer.sensorautorotation.processes.ui.instructions;

import samuelpalmer.sensorautorotation.R;

class InstructionsPages {
	public final InstructionsPage[] PAGES = new InstructionsPage[] {
			new InstructionsPage(R.string.instructions_introduction_heading, R.string.instructions_introduction_contents, R.string.app_name),
			new InstructionsPage(R.string.instructions_modes_heading, R.string.instructions_modes_contents, R.string.screen_rotation_enabled, R.string.screen_rotation_disabled_portrait),
			new InstructionsPage(R.string.instructions_toggling_heading, R.string.instructions_toggling_contents),
			new InstructionsPage(R.string.instructions_auto_rotation_heading, R.string.instructions_auto_rotation_contents, R.string.screen_rotation_enabled, R.string.app_name),
			new InstructionsPage(R.string.instructions_position_heading, R.string.instructions_position_contents),
			new InstructionsPage(R.string.instructions_position_changing_heading, R.string.instructions_position_changing_contents, R.string.action_rotate_left, R.string.action_rotate_right),
			new InstructionsPage(R.string.instructions_overridden_heading, R.string.instructions_overridden_contents, R.string.app_name, R.string.preference_forced_apps_title),
			new InstructionsPage(R.string.instructions_uninstalling_heading, R.string.instructions_uninstalling_contents, R.string.action_uninstall, R.string.app_name),
			new InstructionsPage(R.string.instructions_help_heading, R.string.instructions_help_contents, R.string.action_rotate_left, R.string.action_rotate_right)
	};
}
