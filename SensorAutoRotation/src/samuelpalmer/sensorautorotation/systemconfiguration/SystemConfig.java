package samuelpalmer.sensorautorotation.systemconfiguration;

public class SystemConfig {
	
	private UntypedSystemConfig untyped;

	public SystemConfig(UntypedSystemConfig untyped) {
		this.untyped = untyped;
	}
	
	public TypedSystemConfig<Boolean> asBoolean() {
		return new ConvertingTypedSystemConfig<Boolean>(untyped, new BooleanSystemConfigTypeConverter(new IntegerSystemConfigTypeConverter()));
	}
	
}
