package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ChooseSideActivity extends DialogActivity {

	private Dialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		dialog = new AlertDialog.Builder(this)
			.setView(getLayoutInflater().inflate(R.layout.fragment_choose_side, null))
			.show();
		
		dialog.findViewById(R.id.choose_side_left).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(UserSide.LEFT);
			}
		});
		
		dialog.findViewById(R.id.choose_side_right).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startApp(UserSide.RIGHT);
			}
		});
		
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				finish();
			}
		});
	}
	
	private void startApp(UserSide userSide) {
		new ServiceSynchroniser(ApplicationSettings.instance()).enableService(userSide);
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(DialogActivity.class.getSimpleName(), "Destroying");
		
		//Prevents a window leak error
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		/*
		 * Since this activity can be shown on the lock screen,
		 * leaving the activity open when you lock your phone would
		 * result in it being shown when you next unlock your phone.
		 * (At least in Android 7.1.1.)
		 *
		 * Finishing the activity on stop seems to fix this nicely.
		 * Note that this only works because the activity is declared as
		 * supporting screen size, orientation & layout config changes. If it wasn't,
		 * changes to those properties would also result in the activity finishing.
		 */
		finish();
	}
	
}
