package samuelpalmer.sensorautorotation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class StartServiceTask extends AsyncTask<Void, Void, Void> {
	
	private final Context context;
	private ProgressDialog dialog;

	public StartServiceTask(Context context) {
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		dialog = new ProgressDialog(context);
		dialog.setMessage("Starting service");
		dialog.show();
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		new ServiceController(context).start();
        
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		dialog.dismiss();
	}

}
