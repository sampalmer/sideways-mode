package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class ColourGetterCompat {
	public static int getColor(Context context, int id) throws NotFoundException {
		if (VERSION.SDK_INT >= VERSION_CODES.M)
			return context.getResources().getColor(id, null);
		else
			//noinspection deprecation
			return context.getResources().getColor(id);
	}
}
