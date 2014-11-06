package samuelpalmer.sensorautorotation;

/**
 * Represents a non-bug application error.
 */
public class EnvironmentException extends Exception {
	
	private static final long serialVersionUID = 7336595908877029674L;

	public EnvironmentException(String message) {
		super(message);
	}

	public EnvironmentException(Exception cause) {
		super(cause.getMessage(), cause);
	}
}
