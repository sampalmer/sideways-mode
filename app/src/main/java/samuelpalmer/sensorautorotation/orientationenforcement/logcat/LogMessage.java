package samuelpalmer.sensorautorotation.orientationenforcement.logcat;

class LogMessage {
	public static final int TAG_START_INDEX = 2;
	private final String line;

	public LogMessage(String line) {
		this.line = line;
	}

	public String tag() {
		int endIndex = tagEndIndex();
		String padded = line.substring(TAG_START_INDEX, endIndex);
		return padded.trim(); //Logcat seems to output with a minimum padding before the brackets
	}

	public String message() {
		int colonIndex = line.indexOf(':', tagEndIndex());
		int index = colonIndex + 2;
		return line.substring(index);
	}

	private int tagEndIndex() {
		return line.indexOf('(', TAG_START_INDEX);
	}
}
