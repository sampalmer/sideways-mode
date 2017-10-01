package samuelpalmer.sensorautorotation.testing;

import android.app.ActivityManager;

import samuelpalmer.sensorautorotation.CurrentProcess;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Useful for testing behaviour when the OS is low on RAM.
 * Put the following attribute on your &lt;application&gt; in your manifest:
 *
 * android:largeHeap="true"
 */
@SuppressWarnings("unused")
public class MemoryHog {

	@SuppressWarnings("FieldCanBeLocal")
	private final long[] data;

	public MemoryHog() {
		int maxMemoryMegabytes = ((ActivityManager) CurrentProcess.context().getSystemService(ACTIVITY_SERVICE)).getLargeMemoryClass();
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
