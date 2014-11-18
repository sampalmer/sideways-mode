package samuelpalmer.sensorautorotation.unused;

import java.io.IOException;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.IntegerUtilities;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.UserErrorHandler;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class CameraOrientationCalculator {
	
	private Camera camera;
	private Context context;
	private Screen screen;
	
	public CameraOrientationCalculator(Context context, Screen screen) throws EnvironmentException {
		this.context = context;
		this.screen = screen;
		
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 14){
			
			SurfaceView preview = new SurfaceView(context);
			
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
		            WindowManager.LayoutParams.WRAP_CONTENT,
		            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
		            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
		            PixelFormat.TRANSLUCENT);    
			preview.setZOrderOnTop(true);
			wm.addView(preview, params);
			
			preview.setVisibility(View.VISIBLE);
			
			camera = openFrontCamera();
			if (camera != null) {
				Parameters cameraParams = camera.getParameters();
				int maxNumDetectedFaces = cameraParams.getMaxNumDetectedFaces();
				boolean supportsFaceDetection = maxNumDetectedFaces > 0;
				if (supportsFaceDetection) {
					camera.setFaceDetectionListener(faceDetectionListener);
					
		            try {
		            	camera.setPreviewDisplay(preview.getHolder());
		            	camera.startPreview();
		            }
		            catch (IOException e) {
		                e.printStackTrace();
		            }
	
					camera.takePicture(null, null, new PictureCallback() {
						
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							
						}
					});
				}
			}
		}
	}
	
	private Camera openFrontCamera() {
	    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    int cameraCount = Camera.getNumberOfCameras();
	    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
	        Camera.getCameraInfo(camIdx, cameraInfo);
	        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	            try {
	                return Camera.open(camIdx);
	            } catch (RuntimeException e) {
	                return null;
	            }
	        }
	    }
	    
	    return null;
	}
	
	private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
		
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			if (faces.length > 0) {
				Face face = faces[0];
				Point nose = new Point((face.leftEye.x + face.rightEye.x) / 2, (face.leftEye.y + face.rightEye.y) / 2);
				Point mouthOffset = new Point(face.mouth.x - nose.x, face.mouth.y - nose.y);
				int angle = IntegerUtilities.wrap((int)(Math.atan2(mouthOffset.y, mouthOffset.x) * 180 / Math.PI), 360);
				
				Orientation result = new Orientation(angle / 90);
				try {
					screen.propose(result);
				} catch (EnvironmentException e) {
					new UserErrorHandler(context).handle(e);
				}
			}
		}
		
	};
	
	public void setDevice(Orientation newDevice) throws EnvironmentException {
		return;
	}

	public void stop() {
		if (camera != null)
			camera.release();
	}

}
