package samuelpalmer.sensorautorotation.facedetection;

import java.io.IOException;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.ExternalResource;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.UserErrorHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class CameraOrientationCalculator extends ExternalResource {

	private static final int OPTIMAL_WIDTH = 160;
	private boolean surfaceAvailable;
	
	private final class CallbackImplementation implements Callback {
		

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			surfaceAvailable = false;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			logger.showMessage("Surface created");
			
			surfaceAvailable = true;
			
			if (detecting)
				takePicture();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}
	}

	private SurfaceView preview;
	private Context context;
	private WindowManager wm;
	private Camera camera;
	private Integer cameraIndex;
	private TimingLogger logger;

	public CameraOrientationCalculator(Context context, TimingLogger logger) {
		this.context = context;
		this.logger = logger;
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}
	
	private UpdatingEventRegistrar<Orientation> onDetected = new UpdatingEventRegistrar<Orientation>(new UserErrorHandler(context));
	private boolean detecting;
	public EventRegistrar<Orientation> onDetected() {
		return onDetected;
	}
	
	@Override
	protected void start() throws EnvironmentException {
	}

	@Override
	protected void stop() throws EnvironmentException {
	}
	
	public void detect() {
		logger.showMessage("Started");
		
		cameraIndex = tryFindFrontCameraIndex();
		camera = null;
		
		startOverlay();
		
		if (!detecting && surfaceAvailable)
			takePicture();
		
		detecting = true;
	}
	
	private void detected(Orientation result) {
		onDetected.report(result);
		detecting = false;
		
		stopOverlay();
		
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void startOverlay() {
		preview = new SurfaceView(context);
		SurfaceHolder holder = preview.getHolder();
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(new CallbackImplementation());

		LayoutParams params = new LayoutParams(1, 1, LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
		wm.addView(preview, params);

		logger.showMessage("Added overlay");
	}
	
	private void stopOverlay() {
		wm.removeView(preview);
	}
	
	private void setCamera() {
		if (cameraIndex == null) {
			logger.showMessage("No front camera");
			return;
		}

		try {
			camera = Camera.open(cameraIndex);
		} catch (RuntimeException e) {
			logger.showMessage("Camera not available: " + e.getMessage());
			return;
		}

		logger.showMessage("Opened camera");
	}

	private void takePicture() {
		setCamera();
		if (camera == null) {
			detected(null);
			return;
		}

		configureCamera();
		
		new Handler().postDelayed(exposureCallback, 1000);
	}

	private Runnable exposureCallback = new Runnable() {
		@Override
		public void run() {
			logger.showMessage("Set exposure");
			
			if (camera != null)
				camera.setOneShotPreviewCallback(pictureTaken);
		}
	};
	
	private final PreviewCallback pictureTaken = new PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] nv21, Camera camera) {
			logger.showMessage("Took picture");

			Bitmap image = new Nv21Decoder(nv21, camera).decode();
			
			logger.showMessage("Decoded picture");

			image = BitmapExtensions.scaleToWidth(image, OPTIMAL_WIDTH);
			
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(cameraIndex, info);
			int cameraOrientationDegrees = info.orientation;
			image = BitmapExtensions.rotatedClockwise(image, cameraOrientationDegrees / 90);
			
			logger.showMessage("Processed picture");
			
			try {
				new PicturePreview(context, image).ensureStarted();
			} catch (EnvironmentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.showMessage("Showed preview");
			
			Orientation result = new FaceOrientationDetector(image).detect();

			logger.showMessage("Did face detection: " + result);
			if (result != null)
				Toast.makeText(context, "Found face", Toast.LENGTH_SHORT).show();
			
			detected(result);
		}
	};
	
	private void configureCamera() {
		mute();
		
		Parameters parameters = camera.getParameters();
		Size optimalSize = new OptimalSizeChooser(parameters.getSupportedPreviewSizes(), OPTIMAL_WIDTH).choose();
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		camera.setParameters(parameters);
		
		try {
			camera.setPreviewDisplay(preview.getHolder());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		camera.startPreview();
		logger.showMessage("Started preview");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void mute() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			camera.enableShutterSound(false);
	}

	private Integer tryFindFrontCameraIndex() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);

			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				return camIdx;
		}

		logger.showMessage("Couldn't find front camera");
		return null;
	}

}
