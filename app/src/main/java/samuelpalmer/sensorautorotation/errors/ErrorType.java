package samuelpalmer.sensorautorotation.errors;

class ErrorType {
	
	private static final ErrorType[] all = new ErrorType[] {
		new ErrorType("generic", new GenericError()),
		new ErrorType("sensor", new SensorError())
	};
	
	private final String code;
	private final EnvironmentError instance;
	
	public static EnvironmentError newInstance(String code) {
		for (ErrorType type : all)
			if (type.code.equals(code))
				return type.instance.newInstance();
		
		throw new RuntimeException("Didn't find an error type for code " + code);
	}
	
	private ErrorType(String code, EnvironmentError instance) {
		this.code = code;
		this.instance = instance;
	}
	
}
