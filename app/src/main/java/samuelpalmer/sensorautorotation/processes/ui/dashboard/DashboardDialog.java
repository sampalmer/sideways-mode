package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;


class DashboardDialog extends AlertDialog {

	private final Listener onStart;
	private final Listener onStop;

	public DashboardDialog(Context context, Listener onStart, Listener onStop) {
		super(context);
		this.onStart = onStart;
		this.onStop = onStop;
		setView(new DashboardView(context, null));
	}

	@Override
	protected void onStart() {
		super.onStart();
		onStart.onChange(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		onStop.onChange(this);
	}

	public interface Listener {
		void onChange(Dialog dialog);
	}

}
