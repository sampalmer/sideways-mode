package samuelpalmer.sensorautorotation.unused.facedetection;

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
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Handler;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class CameraOrientationCalculator extends ExternalResource {

	private static final int OPTIMAL_WIDTH = 160;
	
	private boolean surfaceCreated;
	
	private final class CallbackImplementation implements SurfaceTextureListener {

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			surfaceCreated = true;
			logger.showMessage("Surface created");
			startPreview();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			surfaceCreated = false;
			logger.showMessage("Surface destroyed");
			
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			
		}
	}

	private TextureView preview;
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

	private boolean exposureSet;
	public EventRegistrar<Orientation> onDetected() {
		return onDetected;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		logger.showMessage("Start called");
		
		cameraIndex = tryFindFrontCameraIndex();
		surfaceCreated = false;
		exposureSet = false;
		camera = null;
		
		setCamera();
		
		if (camera != null) {
			startOverlay();
			configureCamera();
		}
	}

	@Override
	protected void stop() throws EnvironmentException {
		if (camera != null) {
			camera.release();
			camera = null;
		}
		
		stopOverlay();
	}
	
	public void detect() {
		if (camera == null) {
			detected(null);
			return;
		}
		
		logger.showMessage("Detect called");
		
		if (!detecting && surfaceCreated && exposureSet)
			takePicture();
		else
			detecting = true;
	}
	
	private void detected(Orientation result) {
		onDetected.report(result);
		detecting = false;
	}
	
	private void startOverlay() {
		preview = new TextureView(context);
		preview.setSurfaceTextureListener(new CallbackImplementation());
		
		//TODO: Change preview dimensions to match those from the camera
		LayoutParams params = new LayoutParams(8, 16, Integer.MIN_VALUE, Integer.MIN_VALUE, LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_HARDWARE_ACCELERATED, PixelFormat.TRANSPARENT);
		params.alpha = 0;
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

	private void startPreview() {
		try {
			camera.setPreviewTexture(preview.getSurfaceTexture());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		camera.startPreview();
		logger.showMessage("Started preview");
		
		new Handler().postDelayed(exposureCallback, 1000);
	}

	private Runnable exposureCallback = new Runnable() {
		@Override
		public void run() {
			logger.showMessage("Set exposure");
			
			exposureSet = true;
			if (detecting)
				takePicture();
		}
	};
	
	private void takePicture() {
		Size previewSize = camera.getParameters().getPreviewSize();
		double scaleMultiplier = (double)OPTIMAL_WIDTH / previewSize.width;
		int height = (int) (previewSize.height * scaleMultiplier);
		
		Bitmap image = Bitmap.createBitmap(OPTIMAL_WIDTH, height, Config.RGB_565);
		preview.getBitmap(image);
		
		logger.showMessage("Took picture");
		
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraIndex, info);
		int cameraOrientationDegrees = info.orientation;
		image = BitmapExtensions.rotatedCounterclockwise(image, cameraOrientationDegrees / 90);
		
		logger.showMessage("Rotated picture");
		
		try {
			new PicturePreview(context, image).ensureStarted();
		} catch (EnvironmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.showMessage("Showed preview");
		
		Orientation result = new FaceOrientationDetector(context, image).detect();

		logger.showMessage("Did face detection: " + result);
		
		detected(result);
	}
	
	private void configureCamera() {
		mute();
		
		Parameters parameters = camera.getParameters();
		Size optimalSize = new OptimalSizeChooser(parameters.getSupportedPreviewSizes(), OPTIMAL_WIDTH).choose();
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		camera.setParameters(parameters);
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
