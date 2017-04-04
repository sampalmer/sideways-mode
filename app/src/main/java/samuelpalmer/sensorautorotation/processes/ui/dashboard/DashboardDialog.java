package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import samuelpalmer.sensorautorotation.R;

public class DashboardDialog extends DialogFragment {

	private Context context;

	/**
	 * An inner class used to pass context into the dialog.
	 */
	public static class Builder {

		private Context context;

		public Builder(Context context){
			this.context = context;
		}

		public DashboardDialog create() {
			return new DashboardDialog().setContext(this.context);
		}

	}

	@Override
	public Dialog onCreateDialog(Bundle savedState){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.context);
		View customView = LayoutInflater.from(alertBuilder.getContext()).inflate(R.layout.dashboard, null);
		alertBuilder.setView(customView);

		AlertDialog alertDialog = alertBuilder.create();
		DashboardCoordinator.created(alertDialog);
		return alertDialog;
	}

	private DashboardDialog setContext(Context context){
		this.context = context;
		return this;
	}


	//Note that the below callbacks only apply when the dialog is hosted in a fragment
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		dialogIsGoing();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		dialogIsGoing();
	}

	private void dialogIsGoing() {
		DashboardCoordinator.destroyed();

		Activity activity = getActivity();
		if (activity != null)
			activity.finish();
	}

}
