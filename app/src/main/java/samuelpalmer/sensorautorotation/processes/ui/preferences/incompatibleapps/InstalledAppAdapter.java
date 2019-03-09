package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;

class InstalledAppAdapter extends ArrayAdapter<InstalledApp> {

    public InstalledAppAdapter(Context context) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedInstalledApp result = (CheckedInstalledApp) convertView;
        if (result == null)
            result = new CheckedInstalledApp(getContext());
        
        InstalledApp item = getItem(position);
        result.setItem(item);

        return result;
    }

}
