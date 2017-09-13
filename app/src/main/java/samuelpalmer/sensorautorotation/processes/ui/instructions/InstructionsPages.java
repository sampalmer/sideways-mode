package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.R;

class InstructionsPages {
	
	public static final PageFactory[] PAGES = new PageFactory[] {
		
		new PageFactory() {
			@Override
			public Fragment makePage() {
				return InstructionsPageFragment.newInstance(R.string.instructions_introduction_heading, R.string.instructions_introduction_contents, R.string.app_name_full);
			}
		},
		
		new PageFactory() {
			@Override
			public Fragment makePage() {
				return InstructionsPageFragment.newInstance(R.string.instructions_position_heading, R.string.instructions_position_contents);
			}
		},
		
		new PageFactory() {
			@Override
			public Fragment makePage() {
				return InstructionsPageFragment.newInstance(R.string.instructions_position_changing_heading, R.string.instructions_position_changing_contents, R.string.action_rotate_left, R.string.action_rotate_right);
			}
		},
		
		new PageFactory() {
			@Override
			public Fragment makePage() {
				return InstructionsPageFragment.newInstance(R.string.instructions_uninstalling_heading, R.string.instructions_uninstalling_contents, R.string.action_uninstall, R.string.app_name_full);
			}
		},
		
		new PageFactory() {
			@Override
			public Fragment makePage() {
				return InstructionsPageFragment.newInstance(R.string.instructions_help_heading, R.string.instructions_help_contents, R.string.action_rotate_left, R.string.action_rotate_right);
			}
		}
		
	};
	
}
