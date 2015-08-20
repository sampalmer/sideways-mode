package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.LockScreenStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;
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
				.setSmallIcon(params.user.iconId())
				.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
				.setContentTitle(StringResource.get(R.string.app_name))
				.setContentText(StringResource.get(params.user.descriptionId()))
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

		configureButton(R.id.stop_button, RotationService.ACTION_STOP, "x.svg", R.string.action_stop);
		configureButton(R.id.toggle_mode_button, RotationService.ACTION_TOGGLE, params.mode.iconSvgFileName(params.overridden), R.string.action_toggle_auto_rotation);
		configureButton(R.id.rotate_left_button, RotationService.ACTION_ROTATE_LEFT, "ic_rotate_left.svg", R.string.action_rotate_left);
		configureButton(R.id.rotate_right_button, RotationService.ACTION_ROTATE_RIGHT, "ic_rotate_right.svg", R.string.action_rotate_right);

		notification.contentView = content;
	}

	private void configureButton(int id, String action, String iconFilename, int contentDescriptionId) {
		content.setOnClickPendingIntent(id, RotationServiceReceiver.getPendingIntent(action));

		if (VERSION.SDK_INT >= 15)
			content.setContentDescription(id, StringResource.get(contentDescriptionId));

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

		int lengthInDp = 32; //As per docs: http://developer.android.com/reference/android/app/Notification.Builder.html#addAction(int, java.lang.CharSequence, android.app.PendingIntent)

		Bitmap bitmap = Bitmap.createBitmap(
			//Not using densityDpi since it's sometimes 0 (seen in Genymotion 240x320 120dpi display, also http://stackoverflow.com/q/27590213/238753)
			dpToPx(lengthInDp, display.xdpi),
			dpToPx(lengthInDp, display.ydpi),
			Bitmap.Config.ARGB_8888
		);

		Canvas canvas = new Canvas(bitmap);
		svg.setDocumentWidth(bitmap.getWidth());
		svg.setDocumentHeight(bitmap.getHeight());

		int colour;
		if (styles.actionButtonStyle.colour != null)
			colour = styles.actionButtonStyle.colour;
		else {
			if (styles.titleStyle.colour != null) //Android 4.0 doesn't have notification actions
				colour = Color.rgb(Color.red(styles.titleStyle.colour), Color.green(styles.titleStyle.colour), Color.blue(styles.titleStyle.colour));
			else
				colour = Color.BLACK;

			colour = Color.argb((int) (255 * .8), Color.red(colour), Color.green(colour), Color.blue(colour));
		}

		svg.setColorFilter(new ColorMatrixColorFilter(new float[] {
			0, 0, 0, 0, Color.red(colour),
			0, 0, 0, 0, Color.green(colour),
			0, 0, 0, 0, Color.blue(colour),
			0, 0, 0, Color.alpha(colour) / 255.0f, 0
		}));

		svg.renderToCanvas(canvas);

		content.setImageViewBitmap(id, bitmap);
	}

	private int dpToPx(int lengthInDp, float dpi) {
		return Math.round(lengthInDp * (dpi / 160));
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
