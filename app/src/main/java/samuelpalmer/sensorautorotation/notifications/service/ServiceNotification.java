package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DrawFilter;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.caverock.androidsvg.SVGParser;

import java.io.IOException;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.LockScreenStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ServiceNotification {

	private final NotificationPriority priority;
	private final NotificationParameters params;
	private final NotificationStyles styles;
	private NotificationBuilderCompat builder;
	private RemoteViews content;

	public ServiceNotification(NotificationPriority priority, NotificationParameters notificationParameters) {
		this.priority = priority;
		params = notificationParameters;
		styles = new NotificationStyles();
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build() {
		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		builder = NotificationBuilderCompat.create()
				.setSmallIcon(params.mode.iconResourceId(params.overridden))
				.setContentIntent(RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE))
				.setContentTitle(params.summary)
				.setContentText(StringResource.get(params.mode.actionDescriptionResourceId()))
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		if (params.overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		priority.apply(builder);

		hideTimestamp();
		hideFromLockScreen();

		Notification notification = builder.build();
		addActions(notification);

		return notification;
	}

	private void addActions(Notification notification) {
		content = new RemoteViews(Process.context().getPackageName(), R.layout.notification);
		content.removeAllViews(R.id.notification_main_column);
		content.addView(R.id.notification_main_column, notification.contentView);

		configureButton(R.id.rotate_left_button, RotationService.ACTION_ROTATE_LEFT, "ic_rotate_left.svg");
		configureButton(R.id.rotate_right_button, RotationService.ACTION_ROTATE_RIGHT, "ic_rotate_right.svg");

		notification.contentView = content;
	}

	private void configureButton(int id, String action, String iconFilename) {
		content.setOnClickPendingIntent(id, RotationServiceReceiver.getPendingIntent(action));

		// Read an SVG from the assets folder
		SVG svg;
		//noinspection TryWithIdenticalCatches
		try {
			svg = SVG.getFromAsset(Process.context().getAssets(), iconFilename);
		} catch (SVGParseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		DisplayMetrics display = new DisplayMetrics();
		SystemServices.windowManager().getDefaultDisplay().getMetrics(display);

		int lengthInDp = 50;
		int lengthInPx = lengthInDp * (display.densityDpi / 160);

		Bitmap bitmap = Bitmap.createBitmap(
			lengthInPx,
			lengthInPx,
			Bitmap.Config.ARGB_8888
		);

		Canvas canvas = new Canvas(bitmap);

		svg.setDocumentWidth(bitmap.getWidth());
		svg.setDocumentHeight(bitmap.getHeight());
		svg.renderToCanvas(canvas);

		content.setInt(id, "setColorFilter", styles.titleStyle.colour);
		content.setImageViewBitmap(id, bitmap);
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

	private void hideFromLockScreen() {
		//Notifications only show on lock screen as of Android 5
		if (Build.VERSION.SDK_INT >= 21) {
			//This seems to be the best we've got: http://stackoverflow.com/questions/27035202/how-to-suppress-notification-on-lock-screen-in-android-5-lollipop-but-let-it-i
			builder.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET);

			if (LockScreenStatus.isActive())
				builder.setPriority(NotificationBuilderCompat.PRIORITY_MIN);
		}
	}
}
