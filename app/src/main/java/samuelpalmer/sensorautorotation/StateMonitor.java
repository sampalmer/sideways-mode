package samuelpalmer.sensorautorotation;

public abstract class StateMonitor<T> extends State {

	private final Listener<T> listener;

	protected StateMonitor(Listener<T> listener) {
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
