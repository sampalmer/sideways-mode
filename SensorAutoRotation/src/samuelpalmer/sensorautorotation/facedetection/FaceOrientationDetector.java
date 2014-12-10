package samuelpalmer.sensorautorotation.facedetection;

import samuelpalmer.sensorautorotation.Orientation;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
import android.widget.Toast;

public class FaceOrientationDetector {
	private Bitmap image;
	private Context context;

	public FaceOrientationDetector(Context context, Bitmap image) {
		this.context = context;
		this.image = image;
	}
	
	public Orientation detect() {
		for (int clockwiseRotations = 0; clockwiseRotations < 4; ++clockwiseRotations) {
			Face face = hasFace(clockwiseRotations);
			
			if (face != null) {
				Orientation orientation = new Orientation(clockwiseRotations);
				
				String message = "Found " + orientation + " face (" + Math.round(face.confidence() * 10000.0) / 100.0 + "% confident)";
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
				Log.i("FaceOrientationDetector", message);
				
				return orientation;
			}
		}
		
		return null;
	}
	
	private Face hasFace(int clockwiseRotations) {
		Bitmap rotatedImage = BitmapExtensions.rotatedCounterclockwise(image, clockwiseRotations);

		final int MAX_FACES = 1;
		Face[] faces = new Face[MAX_FACES];
		FaceDetector detector = new FaceDetector(rotatedImage.getWidth(), rotatedImage.getHeight(), faces.length);
		int detectedFaces = detector.findFaces(rotatedImage, faces);
		boolean found = detectedFaces > 0;
		if (!found)
			return null;
		
		Face face = faces[0];
		
		return face;
	}
}
