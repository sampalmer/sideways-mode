package samuelpalmer.sensorautorotation.settings.application.remote.processprefs;

import android.net.LocalServerSocket;

import java.io.IOException;

class Semaphore {

	private static final int TIMEOUT_MILLISECONDS = 5000;
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

				if (System.currentTimeMillis() > timeout)
					throw new RuntimeException("Locking took longer than " + TIMEOUT_MILLISECONDS + "ms.");
			}
		} while (lock == null);
	}

	public synchronized void release() {
		if (lock == null)
			throw new RuntimeException("Already released");

		try {
			lock.close();
		} catch (IOException ignored) {}

		lock = null;
	}

}
