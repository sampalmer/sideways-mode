package samuelpalmer.sensorautorotation.instructions;

import android.view.Gravity;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

class InstructionsPages {
	public final InstructionsPage[] PAGES = new InstructionsPage[] {
			new InstructionsPage("rotation.svg", R.string.instructions_introduction, R.string.app_name),
			new InstructionsPage("notification.svg", R.string.instructions_modes, R.string.screen_rotation_enabled, R.string.screen_rotation_disabled_portrait),
			new InstructionsPage("rotation.svg", R.string.instructions_toggling, smallAppClause()),
			new InstructionsPage("portrait_lock.svg", R.string.instructions_fixed_rotation),
			new InstructionsPage("rotation.svg", R.string.instructions_auto_rotation, R.string.screen_rotation_enabled, R.string.app_name),
			new InstructionsPage("person.svg", R.string.instructions_position),
			new InstructionsPage("person.svg", R.string.instructions_position_changing, R.string.app_name),
			new InstructionsPage("rotation_overridden.svg", R.string.instructions_overridden, R.string.app_name, R.string.notification_overridden),
			new InstructionsPage("delete.svg", R.string.instructions_uninstalling, R.string.action_uninstall, R.string.app_name),
			new InstructionsPage("need_help.svg", R.string.instructions_help, R.string.screen_rotation_enabled) {
				@Override
				public void applyText(TextView pageText) {
					super.applyText(pageText);
					pageText.setGravity(Gravity.START);
				}
			}
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
