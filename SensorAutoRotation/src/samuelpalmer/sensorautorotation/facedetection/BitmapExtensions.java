package samuelpalmer.sensorautorotation.facedetection;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapExtensions {
	
	public static Bitmap rotatedClockwise(Bitmap image, int ninetyDegreeRotations) {
		Matrix matrix = new Matrix();
		matrix.postRotate(ninetyDegreeRotations * 90);

		return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
	}

	public static Bitmap scaleToWidth(Bitmap image, int targetWidth) {
		double scaleMultiplier = (double)targetWidth / image.getWidth();
		int height = (int) (image.getHeight() * scaleMultiplier);
		image = Bitmap.createScaledBitmap(image, targetWidth, height, false);
		return image;
	}

}
