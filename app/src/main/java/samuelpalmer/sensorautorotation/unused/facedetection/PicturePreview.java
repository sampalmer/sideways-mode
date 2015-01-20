package samuelpalmer.sensorautorotation.unused.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import samuelpalmer.sensorautorotation.State;

public class PicturePreview extends State {

	private ImageView view;
	private WindowManager wm;

	public PicturePreview(Context context, Bitmap image) {
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		view = new ImageView(context);
		view.setImageBitmap(image);
		view.setScaleType(ScaleType.CENTER);
	}

	@Override
	protected void start() {
		LayoutParams params = new LayoutParams(
				LayoutParams.TYPE_SYSTEM_OVERLAY,
				0,
				PixelFormat.TRANSLUCENT);
//		params.alpha = 0.85f;

		wm.addView(view, params);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				ensureStopped();
			}
		}, 2500);
	}

	@Override
	protected void stop() {
		wm.removeView(view);
	}

}
