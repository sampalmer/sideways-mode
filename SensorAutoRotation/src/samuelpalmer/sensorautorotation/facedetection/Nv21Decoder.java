package samuelpalmer.sensorautorotation.facedetection;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

public class Nv21Decoder {
	private byte[] nv21;
	private Camera camera;

	public Nv21Decoder(byte[] nv21, Camera camera) {
		this.nv21 = nv21;
		this.camera = camera;
	}

	public Bitmap decode() {
		YuvImage yuv = decodeYuv();
		byte[] jpeg = transcodeJpeg(yuv);
		return decodeJpeg(jpeg);
	}

	private YuvImage decodeYuv() {
		Parameters parameters = camera.getParameters();
		Size size = parameters.getPreviewSize();
		return new YuvImage(nv21, parameters.getPreviewFormat(), size.width, size.height, null);
	}
	
	private byte[] transcodeJpeg(YuvImage yuv) {
		ByteArrayOutputStream compressionOutput = new ByteArrayOutputStream();
		boolean success = yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), 10, compressionOutput);
		if (!success)
			throw new RuntimeException("Couldn't compress YUV image to JPEG");
		
		return compressionOutput.toByteArray();
	}

	private Bitmap decodeJpeg(byte[] jpeg) {
		Options opts = new Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap image = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, opts);
		if (image == null)
			throw new RuntimeException("Couldn't decode image");
		
		return image;
	}
}
