package samuelpalmer.sensorautorotation;

public abstract class StateMonitor<T> extends ExternalResource {
	
	private final Listener<T> listener;

	public StateMonitor(Listener<T> listener) {
		this.listener = listener;
	}
	
	@Override
	protected void start() {
		startInternal();
		reportChange(get());
	}
	
	protected abstract void startInternal();
	
	protected abstract T get();
	
	protected void reportChange(T state) {
		listener.stateChanged(state);
	}
	
	public interface Listener<T> {
		void stateChanged(T state);
	}
	
}
