package samuelpalmer.sensorautorotation.unused;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class CameraOrientationCalculator {
	
	private Context context;

	@SuppressWarnings("deprecation")
	public CameraOrientationCalculator(final Context context) {
		this.context = context;
		final SurfaceView preview = new SurfaceView(context);
		SurfaceHolder holder = preview.getHolder();
		
		// deprecated setting, but required on Android versions prior to 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		holder.addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {}
			
			@SuppressWarnings("unused")
			@SuppressLint("NewApi") @Override
			public void surfaceCreated(SurfaceHolder holder) {
				showMessage("Surface created");
				
				Camera camera = null;
				
				try {
					Integer id = findFrontCamera();
					
					if (id != null) {
						try {
							camera = Camera.open(id);
						} catch (RuntimeException e) {
							throw e;
						}
						
						showMessage("Opened camera");
						camera.enableShutterSound(false);
						
						CameraInfo info = new CameraInfo();
					    Camera.getCameraInfo(id, info);
					    int cameraOrientationDegrees = info.orientation;
						
						try {
			            	camera.setPreviewDisplay(holder);
			            }
			            catch (IOException e) {
			                throw new RuntimeException(e);
			            }
					    
					    camera.startPreview();
					    showMessage("Started preview");
					    
					    camera.takePicture(null, null, new PictureCallback() {
							
							@Override
							public void onPictureTaken(byte[] data, Camera camera) {
								showMessage("Took picture");
								camera.release();
								
								Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
								
								//display(image);
								
								//Only works if face is upright :( Maybe keep rotating image to find the orientation.
								int MAX_FACES = 1;
								Face[] faces = new Face[MAX_FACES];
								
								new FaceDetector(image.getWidth(), image.getHeight(), MAX_FACES)
									.findFaces(image, faces);
								
								if (faces[0] != null)
									showMessage("Found face");
							}
						});
					}
				} catch (Exception e) {
					if (camera != null)
						camera.release();
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
		}); 
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutParams params = new LayoutParams(
				1, 1,
				LayoutParams.TYPE_SYSTEM_OVERLAY,
	            0,
	            PixelFormat.UNKNOWN);
		
		wm.addView(preview, params);
	}

	@SuppressWarnings("unused")
	private void display(Bitmap image) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		LayoutParams params = new LayoutParams(
				LayoutParams.TYPE_SYSTEM_OVERLAY,
	            0,
	            PixelFormat.TRANSLUCENT);
		ImageView view = new ImageView(context);
		view.setImageBitmap(image);
		view.setAlpha(0.7f);
		view.setVisibility(View.VISIBLE);
		wm.addView(view, params);
	}
	
	private void showMessage(String message) {
		Log.i("Camera", message);
	}
	
	private Integer findFrontCamera() {
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

}
