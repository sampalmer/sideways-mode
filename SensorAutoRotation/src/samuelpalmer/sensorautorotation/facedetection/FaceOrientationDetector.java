package samuelpalmer.sensorautorotation.facedetection;

import samuelpalmer.sensorautorotation.Orientation;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

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
		Bitmap rotatedImage = BitmapExtensions.rotatedClockwise(image, clockwiseRotations);

		final int MAX_FACES = 1;
		Face[] faces = new Face[MAX_FACES];
		FaceDetector detector = new FaceDetector(rotatedImage.getWidth(), rotatedImage.getHeight(), faces.length);
		int detectedFaces = detector.findFaces(rotatedImage, faces);
		
		return detectedFaces > 0;
	}
}
