package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

public class NotificationStyles {

	public final StyleType titleStyle = new StyleType("TITLE");
	public final StyleType actionButtonStyle = new StyleType("ACTION");

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

				for (StyleType type : new StyleType[] { titleStyle, actionButtonStyle})
					if (text.equals(type.searchTip))
					{
						DisplayMetrics metrics = new DisplayMetrics();
						SystemServices.windowManager().getDefaultDisplay().getMetrics(metrics);
						type.colour = textView.getTextColors().getDefaultColor();
					}
			}
			else if (view instanceof ViewGroup)
				recurseGroup((ViewGroup) view);
		}
	}

	private void extractColors()
	{
		Notification notification = NotificationBuilderCompat.create()
			.setContentTitle(titleStyle.searchTip)
			.addAction(R.drawable.ic_error, actionButtonStyle.searchTip, ComponentType.create(MainActivity.class).pendingIntent())
			.build();

		LinearLayout group = new LinearLayout(Process.context());

		RemoteViews contentView;
		if (VERSION.SDK_INT >= 16)
			contentView = notification.bigContentView;
		else
			contentView = notification.contentView;

		ViewGroup event = (ViewGroup) contentView.apply(Process.context(), group);
		recurseGroup(event);
		group.removeAllViews();

		if (titleStyle.colour == null)
			Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification title colour");
	}

	public class StyleType {
		public Integer colour;
		private String searchTip;

		public StyleType(String tip) {
			searchTip = tip;
		}
	}
}
