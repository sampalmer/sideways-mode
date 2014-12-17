package samuelpalmer.sensorautorotation.unused.facedetection;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapExtensions {
	
	public static Bitmap rotatedCounterclockwise(Bitmap image, int ninetyDegreeRotations) {
		Matrix matrix = new Matrix();
		matrix.postRotate(360 - ninetyDegreeRotations * 90);

		return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
	}

}
