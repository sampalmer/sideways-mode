package samuelpalmer.sensorautorotation;

public abstract class StateMonitor<T> {
	
	private final Listener<T> listener;

	public StateMonitor(Listener<T> listener) {
		this.listener = listener;
	}
	
	public void start() {
		startInternal();
		reportChange(get());
	}
	
	protected abstract void startInternal();
	
	public abstract void stop();
	
	protected abstract T get();
	
	protected void reportChange(T state) {
		listener.stateChanged(state);
	}
	
	public interface Listener<T> {
		void stateChanged(T state);
	}
	
}
