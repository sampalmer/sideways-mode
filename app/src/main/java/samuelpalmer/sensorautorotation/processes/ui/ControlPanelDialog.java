package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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

	@Override
	public Dialog onCreateDialog(Bundle savedState){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.context);
		View customView = LayoutInflater.from(alertBuilder.getContext()).inflate(R.layout.control_panel, null);
		alertBuilder.setView(customView);
		AlertDialog alertDialog = alertBuilder.create();

		Window window = alertDialog.getWindow();
		window.setFormat(PixelFormat.TRANSLUCENT);

		WindowManager.LayoutParams attributes = window.getAttributes();
		attributes.alpha = 0.90f;
		window.setAttributes(attributes);

		return alertDialog;
	}

	private ControlPanelDialog setContext(Context context){
		this.context = context;
		return this;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		Activity activity = getActivity();
		if (activity != null)
			activity.finish();
	}
}
