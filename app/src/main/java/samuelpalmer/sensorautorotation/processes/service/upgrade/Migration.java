package samuelpalmer.sensorautorotation.processes.service.upgrade;

import samuelpalmer.sensorautorotation.StringResource;

class Migration {

	private Integer message;

	public Migration setMessage(int message) {
		this.message = message;
		return this;
	}

	public void upgradeData() {}

	public CharSequence message() {
		if (message != null)
			return StringResource.get(message) + "\n";
		else
			return "";
	}
}
