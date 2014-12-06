package samuelpalmer.sensorautorotation.unused;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.ExternalResource;
import samuelpalmer.sensorautorotation.UserErrorHandler;

public class PicturePreview extends ExternalResource {
	
	private ImageView view;
	private WindowManager wm;
	private Context context;

	public PicturePreview(Context context, Bitmap image) {
		this.context = context;
		wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		
		view = new ImageView(context);
		view.setImageBitmap(image);
		view.setAlpha(0.7f);
		view.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void start() throws EnvironmentException {
		LayoutParams params = new LayoutParams(
				LayoutParams.TYPE_SYSTEM_OVERLAY,
	            0,
	            PixelFormat.TRANSLUCENT);
		
		wm.addView(view, params);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				try {
					ensureStopped();
				} catch (EnvironmentException e) {
					new UserErrorHandler(context).handle(e);
				}
			}
		}, 5000);
	}

	@Override
	protected void stop() throws EnvironmentException {
		wm.removeView(view);
	}

}
