package samuelpalmer.memoryhog;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Useful for testing behaviour when the OS is low on RAM.
 * Put the following attribute on your &lt;application&gt; in your manifest:
 *
 * android:largeHeap="true"
 */
@SuppressWarnings("unused")
public class MemoryHog {

    private static final String TAG = MemoryHog.class.getSimpleName();

    @SuppressWarnings({"FieldCanBeLocal", "MismatchedReadAndWriteOfArray"})
    private final long[] data;

    public MemoryHog(Context context) {
        int maxMemoryMegabytes = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
        int megabyteCount = maxMemoryMegabytes * 15 / 16; // Requesting all of the available memory results in an OutOfMemoryError, so I'm scaling it back a bit
        long byteCount = fromMegabytes(megabyteCount);
        long longCountLong = byteCount / 8L;
        int longCount = (int) longCountLong;
        if (longCount != longCountLong)
            throw new RuntimeException(String.format("Integer overflow when converting %d from long to int", longCountLong));

        try {
            data = new long[longCount];
        }
        catch (OutOfMemoryError ex) {
            throw new RuntimeException("Not enough memory available to allocate " + maxMemoryMegabytes + "MB. Try reducing the amount you request.", ex);
        }

        // This seems to be needed to make it actually consume the RAM
        for (int i = 0; i < data.length; ++i)
            data[i] = i;
    }

    private long fromMegabytes(int megabytes) {
        return (long)megabytes * 1024L * 1024L;
    }

}
