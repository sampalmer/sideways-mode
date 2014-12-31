package samuelpalmer.sensorautorotation;


public class CompositeState extends State {

	private final State[] resources;

	public CompositeState(State[] resources) {
		this.resources = resources.clone();
	}

	@Override
	protected void start() {
		for (State resource : resources)
			resource.start();
	}

	@Override
	protected void stop() {
		for (int i = resources.length - 1; i >= 0; --i)
			resources[i].stop();
	}

}
