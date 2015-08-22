package samuelpalmer.sensorautorotation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.util.DisplayMetrics;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.util.HashMap;

public abstract class SvgToBitmapFactory {

	private static final HashMap<String, Bitmap> cache = new HashMap<>();

	public static Bitmap load(String iconFilename, int colour) {

		Bitmap cached = cache.get(iconFilename);
		if (cached != null)
			return cached;

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

		svg.setColorFilter(new ColorMatrixColorFilter(new float[] {
				0, 0, 0, 0, Color.red(colour),
				0, 0, 0, 0, Color.green(colour),
				0, 0, 0, 0, Color.blue(colour),
				0, 0, 0, Color.alpha(colour) / 255.0f, 0
		}));

		svg.renderToCanvas(canvas);

		cache.put(iconFilename, bitmap);

		return bitmap;
	}

	private static int dpToPx(int lengthInDp, float dpi) {
		return Math.round(lengthInDp * (dpi / 160));
	}

}
