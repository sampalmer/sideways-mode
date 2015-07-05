package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.net.LocalServerSocket;

import java.io.IOException;

import samuelpalmer.sensorautorotation.BuildConfig;

/**
 * Provides mutual exclusion for multiple processes and/or threads. Note that this class itself
 * isn't "thread-safe"; you shouldn't use one instance in multiple threads. Instead, use a different
 * instance in each thread but with the same identifier.
 */
class Semaphore {

	private static final int TIMEOUT_MILLISECONDS = 10000;
	private LocalServerSocket lock;

	public Semaphore(String identifier) {
		long timeout = System.currentTimeMillis() + TIMEOUT_MILLISECONDS;

		do {
			try {
				lock = new LocalServerSocket(identifier);
			} catch (IOException e) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException ignored) {}

				if (!BuildConfig.DEBUG && System.currentTimeMillis() > timeout)
					throw new RuntimeException("Locking took longer than " + TIMEOUT_MILLISECONDS + "ms.");
			}
		} while (lock == null);
	}

	public void release() {
		if (lock == null)
			throw new RuntimeException("Already released");

		try {
			lock.close();
		} catch (IOException ignored) {}

		lock = null;
	}

}
