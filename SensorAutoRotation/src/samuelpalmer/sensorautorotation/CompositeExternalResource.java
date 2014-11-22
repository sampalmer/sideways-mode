package samuelpalmer.sensorautorotation;


public class CompositeExternalResource extends ExternalResource {
	
	private final ExternalResource[] resources;

	public CompositeExternalResource(ExternalResource[] resources) {
		this.resources = resources.clone();
	}
	
	@Override
	protected void start() throws EnvironmentException {
		for (ExternalResource resource : resources)
			resource.start();
	}

	@Override
	protected void stop() throws EnvironmentException {
		for (int i = resources.length - 1; i >= 0; --i)
			resources[i].stop();
	}

}
