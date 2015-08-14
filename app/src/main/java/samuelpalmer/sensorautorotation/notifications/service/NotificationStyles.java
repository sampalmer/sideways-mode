package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;

public class NotificationStyles {

	private Integer textColor = null;
	private float textSize = 11;
	private final String COLOR_SEARCH_RECURSE_TIP = "SOME_SAMPLE_TEXT";

	private void recurseGroup(ViewGroup viewGroup)
	{
		for (int i = 0; i < viewGroup.getChildCount(); ++i)
		{
			View view = viewGroup.getChildAt(i);

			if (view instanceof TextView)
			{
				TextView textView = (TextView) view;
				String text = textView.getText().toString();

				if (COLOR_SEARCH_RECURSE_TIP.equals(text))
				{
					textColor = textView.getTextColors().getDefaultColor();
					textSize = textView.getTextSize();
					DisplayMetrics metrics = new DisplayMetrics();
					SystemServices.windowManager().getDefaultDisplay().getMetrics(metrics);
					textSize /= metrics.scaledDensity;
				}
			}
			else if (view instanceof ViewGroup)
				recurseGroup((ViewGroup) view);
		}
	}

	private void extractColors()
	{
		try
		{
			Notification notification = NotificationBuilderCompat.create()
				.setContentTitle(COLOR_SEARCH_RECURSE_TIP)
				.setContentText("Utest")
				.build();

			LinearLayout group = new LinearLayout(Process.context());
			ViewGroup event = (ViewGroup) notification.contentView.apply(Process.context(), group);
			recurseGroup(event);
			group.removeAllViews();
		}
		catch (Exception e)
		{
			textColor = android.R.color.black;
		}
	}

	public NotificationStyles() {

	}

}
