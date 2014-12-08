package samuelpalmer.sensorautorotation.facedetection;

import android.graphics.Bitmap;
import android.graphics.Color;
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
//		int[] out = decodeGreyscale();

		int[] out = convertYUV420_NV21toRGB8888(nv21, size().width, size().height);
		
		return Bitmap.createBitmap(out, size().width, size().height, Bitmap.Config.RGB_565);
	}

	private int[] decodeGreyscale() {
		int pixelCount = size().width * size().height;
		int[] out = new int[pixelCount];
		for (int i = 0; i < pixelCount; ++i) {
			int luminance = nv21[i] & 0xFF;
			out[i] = Color.argb(0xFF, luminance, luminance, luminance);
		}
		return out;
	}

	private Size size() {
		Parameters parameters = camera.getParameters();
		Size size = parameters.getPreviewSize();
		return size;
	}

	/**
	 * Converts YUV420 NV21 to RGB8888
	 * 
	 * @param data byte array on YUV420 NV21 format.
	 * @param width pixels width
	 * @param height pixels height
	 * @return a RGB8888 pixels int array. Where each int is a pixels ARGB. 
	 */
	public static int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
	    int size = width*height;
	    int offset = size;
	    int[] pixels = new int[size];
	    int u, v, y1, y2, y3, y4;

	    // i percorre os Y and the final pixels
	    // k percorre os pixles U e V
	    for(int i=0, k=0; i < size; i+=2, k+=2) {
	        y1 = data[i  ]&0xff;
	        y2 = data[i+1]&0xff;
	        y3 = data[width+i  ]&0xff;
	        y4 = data[width+i+1]&0xff;

	        u = data[offset+k  ]&0xff;
	        v = data[offset+k+1]&0xff;
	        u = u-128;
	        v = v-128;

	        pixels[i  ] = convertYUVtoRGB(y1, u, v);
	        pixels[i+1] = convertYUVtoRGB(y2, u, v);
	        pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
	        pixels[width+i+1] = convertYUVtoRGB(y4, u, v);

	        if (i!=0 && (i+2)%width==0)
	            i+=width;
	    }

	    return pixels;
	}

	private static int convertYUVtoRGB(int y, int u, int v) {
	    int r,g,b;

	    r = y + (int)1.402f*v;
	    g = y - (int)(0.344f*u +0.714f*v);
	    b = y + (int)1.772f*u;
	    r = r>255? 255 : r<0 ? 0 : r;
	    g = g>255? 255 : g<0 ? 0 : g;
	    b = b>255? 255 : b<0 ? 0 : b;
	    return 0xff000000 | (b<<16) | (g<<8) | r;
	}
}
