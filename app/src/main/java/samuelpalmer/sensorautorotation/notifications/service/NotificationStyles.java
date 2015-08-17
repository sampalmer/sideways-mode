package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;

public class NotificationStyles {

	public final StyleType titleStyle = new StyleType("TITLE");
	public final StyleType textStyle = new StyleType("TEXT");

	public NotificationStyles() {
		extractColors();
	}

	private void recurseGroup(ViewGroup viewGroup)
	{
		for (int i = 0; i < viewGroup.getChildCount(); ++i)
		{
			View view = viewGroup.getChildAt(i);

			if (view instanceof TextView)
			{
				TextView textView = (TextView) view;
				String text = textView.getText().toString();

				for (StyleType type : new StyleType[] { titleStyle, textStyle })
					if (text.equals(type.searchTip))
					{
						DisplayMetrics metrics = new DisplayMetrics();
						SystemServices.windowManager().getDefaultDisplay().getMetrics(metrics);
						type.size = textView.getTextSize() / metrics.scaledDensity;
						type.colour = textView.getTextColors().getDefaultColor();
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
				.setContentTitle(titleStyle.searchTip)
				.setContentText(textStyle.searchTip)
				.build();

			LinearLayout group = new LinearLayout(Process.context());
			ViewGroup event = (ViewGroup) notification.contentView.apply(Process.context(), group);
			recurseGroup(event);
			group.removeAllViews();
		}
		catch (Exception e) {
			Log.w(NotificationStyles.class.getSimpleName(), e);
		}
	}

	public class StyleType {
		public int colour = Color.BLACK;
		private float size = 11;
		private String searchTip;

		public StyleType(String tip) {
			searchTip = tip;
		}

		public void applyTo(RemoteViews content, int id) {
			content.setTextColor(id, colour);
			content.setFloat(id, "setTextSize", size);
		}
	}
}
