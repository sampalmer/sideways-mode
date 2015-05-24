package samuelpalmer.sensorautorotation.testing;

import samuelpalmer.sensorautorotation.SystemServices;

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
		int maxMemoryMegabytes = SystemServices.activityManager().getLargeMemoryClass();
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
