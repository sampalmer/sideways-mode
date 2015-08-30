package samuelpalmer.sensorautorotation.settings.application.multiprocess;

public class GetValueArgs<T> {

	public String key;
	public T defValue;

	public GetValueArgs() {}

	public GetValueArgs(String key, T defValue) {
		this.key = key;
		this.defValue = defValue;
	}
}
