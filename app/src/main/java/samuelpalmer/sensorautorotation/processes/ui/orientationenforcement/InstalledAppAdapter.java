package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import samuelpalmer.sensorautorotation.R;

class InstalledAppAdapter extends ArrayAdapter<InstalledApp> {

	private final LayoutInflater layoutInflater;

	public InstalledAppAdapter(Context context) {
		super(context, R.layout.forced_apps_item);
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckedInstalledApp result = (CheckedInstalledApp) convertView;
		if (result == null)
			result = (CheckedInstalledApp) layoutInflater.inflate(R.layout.forced_apps_item, parent, false);

		InstalledApp item = getItem(position);
		result.setItem(item);

		return result;
	}

}
