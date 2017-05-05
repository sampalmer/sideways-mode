package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class EnabledFragment extends Fragment {

	private static final int PERMISSION_REQUEST_CODE = 1;

	private final EventHandler enabledControlUpdater = new EventHandler() {
		@Override
		public void update() {
			Boolean isEnabled = enabledSetting.get();
			setControlCheckedStateWithoutTriggeringListener(isEnabled);
		}
	};

	private final SwitchBar.OnSwitchChangeListener enabledSettingUpdater = new SwitchBar.OnSwitchChangeListener() {

		@Override
		public void onSwitchChanged(Switch switchView, boolean isChecked) {
			if (Build.VERSION.SDK_INT >= 23 && isChecked && !Settings.System.canWrite(getContext())) {
				requestSystemSettingsPermission();
				setControlCheckedStateWithoutTriggeringListener(false);
			} else {
				enabledSetting.set(isChecked);
				new ServiceSynchroniser(ApplicationSettings.remote()).updateFromSettings();
			}
		}

	};

	private void setControlCheckedStateWithoutTriggeringListener(Boolean isEnabled) {
		enabledControl.setChecked(isEnabled);
	}

	private samuelpalmer.common.settings.application.Settings.Setting<Boolean> enabledSetting;
	private SwitchBar enabledControl;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enabledSetting = ApplicationSettings.remote().enabled();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_enabled, container, true);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enabledControl = (SwitchBar) view.findViewById(R.id.switch_bar);
 		enabledControl.addOnSwitchChangeListener(enabledSettingUpdater);
	}

	@Override
	public void onStart() {
		super.onStart();

		enabledSetting.subscribe(enabledControlUpdater);
		enabledControlUpdater.update(); //In case it changed while the activity was in the background
	}

	@Override
	public void onStop() {
		super.onStop();

		enabledSetting.unsubscribe(enabledControlUpdater);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void requestSystemSettingsPermission() {
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.environment_error_permission_needed_summary)
				.setMessage(R.string.environment_error_permission_needed_system_settings_message)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO: Handle case where this activity isn't available
						startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getContext().getPackageName(), null)), PERMISSION_REQUEST_CODE);
					}
				})
				.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (Build.VERSION.SDK_INT >= 23 && requestCode == PERMISSION_REQUEST_CODE)
			handleSystemSettingsRequestResult();
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void handleSystemSettingsRequestResult() {
		if (Settings.System.canWrite(getContext()))
			new ServiceSynchroniser(ApplicationSettings.remote()).enableService();
		else
			Toast
				.makeText(getContext(), R.string.warning_permission_not_granted_system_settings, Toast.LENGTH_LONG)
				.show();
	}
	
	public void setEnabled() {
		enabledControl.setChecked(true);
	}
}
