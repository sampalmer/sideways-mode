package samuelpalmer.sensorautorotation.facedetection;

import samuelpalmer.sensorautorotation.Orientation;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;

public class FaceOrientationDetector {
	private Bitmap image;

	public FaceOrientationDetector(Bitmap image) {
		this.image = image;
	}
	
	public Orientation detect() {
		for (int clockwiseRotations = 0; clockwiseRotations < 4; ++clockwiseRotations)
			if (hasFace(clockwiseRotations))
				return new Orientation(clockwiseRotations);
		
		return null;
	}
	
	private boolean hasFace(int clockwiseRotations) {
		Bitmap rotatedImage = BitmapExtensions.rotatedCounterclockwise(image, clockwiseRotations);

		final int MAX_FACES = 1;
		Face[] faces = new Face[MAX_FACES];
		FaceDetector detector = new FaceDetector(rotatedImage.getWidth(), rotatedImage.getHeight(), faces.length);
		int detectedFaces = detector.findFaces(rotatedImage, faces);
		boolean found = detectedFaces > 0;
		if (!found)
			return false;
		
		Face face = faces[0];
		Log.i("FaceORientation", "Detected face: " + face.confidence() * 100.0 + "% confident");
		return true;
	}
}
