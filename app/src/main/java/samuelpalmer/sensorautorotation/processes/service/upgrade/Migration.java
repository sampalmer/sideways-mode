package samuelpalmer.sensorautorotation.processes.service.upgrade;

import samuelpalmer.sensorautorotation.StringResource;

class Migration {

	private final Integer message;

	public Migration(Integer message) {
		this.message = message;
	}

	public void upgradeData() {}

	public CharSequence message() {
		if (message != null)
			return StringResource.get(message) + "\n";
		else
			return "";
	}
}
