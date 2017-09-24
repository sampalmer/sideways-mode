package samuelpalmer.common.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

public final class NotificationStyles {

	private final StyleType title = new StyleType("TITLE");
	private final StyleType actionButton = new StyleType("ACTION");
	private final Context context;
	private final int exampleIconResource;
	private final int colourArgb;

	/**
	 * You might want to cache the result for performance & memory.
	 */
	public NotificationStyles(Context context, int exampleActionIconResource, int colourArgb) {
		this.context = context;
		this.exampleIconResource = exampleActionIconResource;
		this.colourArgb = colourArgb;
		extractColors();
	}

	public int getActionButtonColour() {
		int result;

		if (actionButton.colour != null)
			result = actionButton.colour;
		else { //Android 4.0 doesn't have notification actions
			if (title.colour != null)
				result = Color.rgb(Color.red(title.colour), Color.green(title.colour), Color.blue(title.colour));
			else
				result = Color.BLACK;

			result = Color.argb((int) (255 * .8), Color.red(result), Color.green(result), Color.blue(result));
		}

		return result;
	}

	private void extractColors()
	{
		NotificationBuilderCompat builder = NotificationBuilderCompat.create(context)
			.setContentTitle(title.searchTip)
			.setColor(colourArgb); //Android N seems to use the colour on the action buttons

		//Add action button
		String actionTitle = actionButton.searchTip;
		PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(""), 0);
		builder.addAction(exampleIconResource, actionTitle, intent);

		//Build
		RemoteViews contentView = getContentView(builder);

		LinearLayout group = new LinearLayout(context);
		ViewGroup event = (ViewGroup) contentView.apply(context, group);
		extractColours(event);
		group.removeAllViews();

		if (title.colour == null)
			Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification title colour");
		if (VERSION.SDK_INT >= 16 && actionButton.colour == null)
			Log.w(NotificationStyles.class.getSimpleName(), "Couldn't find notification action colour");
	}

	private RemoteViews getContentView(NotificationBuilderCompat builder) {
		RemoteViews result = builder.createBigContentView();
		return result == null ? builder.createContentView() : result;
	}

	private void extractColours(ViewGroup viewGroup)
	{
		for (int i = 0; i < viewGroup.getChildCount(); ++i)
		{
			View view = viewGroup.getChildAt(i);
			
			if (view instanceof TextView)
			{
				TextView textView = (TextView) view;
				String text = textView.getText().toString();

				if (text.equals(title.searchTip))
					title.colour = extractTitleColour(textView);
				
				if (text.equals(actionButton.searchTip))
					actionButton.colour = extractActionColour(textView);
			}
			else if (view instanceof ViewGroup)
				extractColours((ViewGroup) view);
		}
	}
	
	private Integer extractTitleColour(TextView textView) {
		return extractTextColour(textView);
	}
	
	private Integer extractActionColour(TextView textView) {
		if (Build.VERSION.SDK_INT <= 19)
			// These versions of Android didn't recolour the icon, so it should be the same as the source image, which should be white.
			return Color.WHITE;
		
		Drawable[] drawables = textView.getCompoundDrawables();
		if (drawables.length > 0) {
			Drawable actionIcon = drawables[0];
			if (actionIcon != null) {
				ColorFilter colorFilter = actionIcon.getColorFilter();
				if (colorFilter instanceof PorterDuffColorFilter)
					try {
						return (int) colorFilter.getClass().getMethod("getColor").invoke(colorFilter);
					} catch (ReflectiveOperationException | ClassCastException ignored) {
					}
			}
		}
		
		// We didn't find the action icon colour, but the text colour should be a good enough approximation
		return extractTextColour(textView);
	}
	
	private int extractTextColour(TextView textView) {
		return textView.getTextColors().getDefaultColor();
	}
	
	private static class StyleType {
		public Integer colour;
		private final String searchTip;

		public StyleType(String tip) {
			searchTip = tip;
		}
	}
}
