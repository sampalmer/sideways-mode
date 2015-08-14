package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.widget.RemoteViews;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
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
		content = new RemoteViews(Process.context().getPackageName(), R.layout.notification);
		configureButton(R.id.rotate_left_button, "ic_rotate_left.svg", RotationService.ACTION_ROTATE_LEFT);
		configureButton(R.id.rotate_right_button, "ic_rotate_right.svg", RotationService.ACTION_ROTATE_RIGHT);

		int icon = params.mode.iconResourceId(params.overridden);
		content.setImageViewResource(R.id.notification_icon, icon);

		content.setTextViewText(R.id.notification_title, params.summary);
		styles.titleStyle.applyTo(content, R.id.notification_title);

		content.setTextViewText(R.id.notification_text, StringResource.get(params.mode.actionDescriptionResourceId()));
		styles.textStyle.applyTo(content, R.id.notification_text);

		builder = NotificationBuilderCompat.create()
				.setSmallIcon(icon)
				.setContentIntent(RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE))
				.setContentTitle(params.summary)
				.setContentText(StringResource.get(params.mode.actionDescriptionResourceId()))
				.setContent(content)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		if (params.overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		priority.apply(builder);

		hideTimestamp();
		hideFromLockScreen();

		return builder.build();
	}

	private void configureButton(int id, String iconFilename, String action) {
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

		Bitmap bitmap = Bitmap.createBitmap(
			50,
			50,
			Bitmap.Config.ARGB_8888
		);

		Canvas canvas = new Canvas(bitmap);
		svg.setDocumentWidth(bitmap.getWidth());
		svg.setDocumentHeight(bitmap.getHeight());
		svg.renderToCanvas(canvas);

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
