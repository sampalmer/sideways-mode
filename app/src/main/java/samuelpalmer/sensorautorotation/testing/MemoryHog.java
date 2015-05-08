package samuelpalmer.sensorautorotation.testing;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Useful for testing behaviour when the OS is low on RAM.
 * Put the following attribute on your &lt;application&gt; in your manifest:
 *
 * android:largeHeap="true"
 */
public class MemoryHog {

	private final long[] data;

	public MemoryHog(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int maxMemoryMegabytes = activityManager.getLargeMemoryClass();
		int byteCount = fromMegabytes(maxMemoryMegabytes - 10);
		int longCount = byteCount / 8;
		data = new long[longCount];

		//This seems to be needed to make it actually consume the RAM
		for (int i = 0; i < data.length; ++i)
			data[i] = i;
	}

	private int fromMegabytes(int megabytes) {
		return megabytes * 1024 * 1024;
	}

}
