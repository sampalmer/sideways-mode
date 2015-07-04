package samuelpalmer.sensorautorotation.settings.application.remote.processprefs;

class SemaphoreLocker {

	private final String semaphoreIdentifier;

	public SemaphoreLocker(String semaphoreIdentifier) {
		this.semaphoreIdentifier = semaphoreIdentifier;
	}

	public <Result> Result lock(Operation<Result> operation) {
		Semaphore lock = null;

		try {
			lock = new Semaphore(semaphoreIdentifier);
			return operation.run();
		}
		finally {
			if (lock != null)
				lock.release();
		}
	}

	public interface Operation<Result> {
		Result run();
	}

}
