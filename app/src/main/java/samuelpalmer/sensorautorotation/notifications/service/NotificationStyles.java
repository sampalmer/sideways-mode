package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.graphics.Color;
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

public final class NotificationStyles {

	private static Integer actionButtonColour;

	public static int getActionButtonColour() {
		if (actionButtonColour != null)
			return actionButtonColour;

		NotificationStyles styles = new NotificationStyles();
		styles.extractColors();

		if (styles.actionButton.colour != null)
			actionButtonColour = styles.actionButton.colour;
		else { //Android 4.0 doesn't have notification actions
			if (styles.title.colour != null)
				actionButtonColour = Color.rgb(Color.red(styles.title.colour), Color.green(styles.title.colour), Color.blue(styles.title.colour));
			else
				actionButtonColour = Color.BLACK;

			actionButtonColour = Color.argb((int) (255 * .8), Color.red(actionButtonColour), Color.green(actionButtonColour), Color.blue(actionButtonColour));
		}

		return actionButtonColour;
	}

	private final StyleType title = new StyleType("TITLE");
	private final StyleType actionButton = new StyleType("ACTION");

	private NotificationStyles(){}

	private void recurseGroup(ViewGroup viewGroup)
	{
		for (int i = 0; i < viewGroup.getChildCount(); ++i)
		{
			View view = viewGroup.getChildAt(i);

			if (view instanceof TextView)
			{
				TextView textView = (TextView) view;
				String text = textView.getText().toString();

				for (StyleType type : new StyleType[] {title, actionButton})
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
			.setContentTitle(title.searchTip)
			.addAction(R.drawable.ic_error, actionButton.searchTip, ComponentType.create(MainActivity.class).pendingIntent())
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

		if (title.colour == null)
			Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification title colour");
	}

	public static class StyleType {
		public Integer colour;
		private final String searchTip;

		public StyleType(String tip) {
			searchTip = tip;
		}
	}
}
