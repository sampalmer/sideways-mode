package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;

public class ControlPanelDialog extends DialogFragment {

	private Context context;

	/**
	 * An inner class used to pass context into the dialog.
	 */
	public static class Builder {

		private Context context;

		public Builder(Context context){
			this.context = context;
		}

		public ControlPanelDialog create() {
			return new ControlPanelDialog().setContext(this.context);
		}

	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedState){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.context);
		alertBuilder.setView(R.layout.control_panel);
		return alertBuilder.create();
	}

	private ControlPanelDialog setContext(Context context){
		this.context = context;
		return this;
	}
}
