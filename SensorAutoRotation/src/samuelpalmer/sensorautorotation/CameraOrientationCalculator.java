package samuelpalmer.sensorautorotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import samuelpalmer.sensorautorotation.unused.PicturePreview;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class CameraOrientationCalculator extends ExternalResource {

	private final class CallbackImplementation implements Callback {
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			showMessage("Surface created");

			if (isStarted())
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
	private Listener listener;
	private Integer cameraIndex;

	public CameraOrientationCalculator(Context context, Listener listener) {
		this.context = context;
		this.listener = listener;
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void start() throws EnvironmentException {
		showMessage("Started");

		camera = null;
		preview = new SurfaceView(context);
		cameraIndex = tryFindFrontCameraIndex();

		SurfaceHolder holder = preview.getHolder();

		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(new CallbackImplementation());

		LayoutParams params = new LayoutParams(1, 1, LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);

		wm.addView(preview, params);

		showMessage("Added overlay");
	}

	@Override
	protected void stop() throws EnvironmentException {
		if (camera != null)
			camera.release();
		wm.removeView(preview);
	}

	private void setCamera() {
		if (cameraIndex == null) {
			showMessage("No front camera"); // Should probably be an
											// EnvironmentException
			return;
		}

		try {
			camera = Camera.open(cameraIndex);
		} catch (RuntimeException e) {
			showMessage("Camera not available: " + e.getMessage());
			return;
		}

		showMessage("Opened camera");
	}

	private void takePicture() {

		setCamera();
		if (camera == null) {
			listener.didntDetect();
			return;
		}

		configureCamera();
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				showMessage("Set exposure");

				camera.setOneShotPreviewCallback(new PreviewCallback() {
					
					@Override
					public void onPreviewFrame(byte[] nv21, Camera camera) {
						showMessage("Took picture");

						if (isStarted()) {
							Parameters parameters = camera.getParameters();
							Size size = parameters.getPreviewSize();
							YuvImage yuv = new YuvImage(nv21, parameters.getPreviewFormat(), size.width, size.height, null);
							
							ByteArrayOutputStream compressionOutput = new ByteArrayOutputStream();
							yuv.compressToJpeg(new Rect(0, 0, size.width, size.height), 10, compressionOutput);
							byte[] jpeg = compressionOutput.toByteArray();
							
							Options opts = new Options();
							opts.inPreferredConfig = Bitmap.Config.RGB_565;
							Bitmap image = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, opts);
							if (image == null)
								throw new RuntimeException("Couldn't decode image");
							
							showMessage("Decoded picture");

							int targetWidth = 160;
							double scaleMultiplier = (double) targetWidth / image.getWidth();
							image = Bitmap.createScaledBitmap(image, targetWidth, (int) (image.getHeight() * scaleMultiplier), false);

							CameraInfo info = new CameraInfo();
							Camera.getCameraInfo(cameraIndex, info);
							int cameraOrientationDegrees = info.orientation;
							image = rotateClockwise(image, cameraOrientationDegrees / 90);

							try {
								new PicturePreview(context, image).ensureStarted();
							} catch (EnvironmentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							showMessage("Showed preview");

							for (int i = 0; i < 4; ++i) {
								Bitmap rotatedImage = rotateClockwise(image, i);

								Face[] faces = new Face[1];

								FaceDetector detector = new FaceDetector(rotatedImage.getWidth(), rotatedImage.getHeight(), faces.length);
								detector.findFaces(rotatedImage, faces);

								if (faces[0] != null) {
									Orientation orientation = new Orientation(i);
//									Toast.makeText(context, "Found face", Toast.LENGTH_SHORT).show();
									showMessage("Found face in " + orientation);
									listener.detected(orientation);
									return;
								}
							}

							showMessage("Did face detection");

							listener.didntDetect();
						}
					}
				});
			}
		}, 500);
	}
	
	private Bitmap rotateClockwise(Bitmap image, int ninetyDegreeRotations) {
		Matrix matrix = new Matrix();
		matrix.postRotate(ninetyDegreeRotations * 90);

		return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
	}
	
	private void configureCamera() {
		mute();
		
		try {
			camera.setPreviewDisplay(preview.getHolder());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		camera.startPreview();
		showMessage("Started preview");
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void mute() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			camera.enableShutterSound(false);
	}

	private void showMessage(String message) {
		long now = System.nanoTime();

		if (lastMessage != null) {
			double durationMs = (now - lastMessage) / 1000000.0;
			message += " (" + Math.round(durationMs) + "ms)";
		}

		lastMessage = now;

		Log.i("Camera", message);
	}

	Long lastMessage;

	private Integer tryFindFrontCameraIndex() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);

			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				return camIdx;
		}

		showMessage("Couldn't find front camera");
		return null;
	}

	public interface Listener {
		void detected(Orientation user);

		void didntDetect();
	}

}
