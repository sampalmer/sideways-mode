package samuelpalmer.sensorautorotation.instructions;

import samuelpalmer.sensorautorotation.StringResource;

public class InstructionsPage {
	public final int heading;
	private final int textId;
	private final Object[] textArguments;

	public InstructionsPage(int heading, int textId, Object... textArguments) {
		this.heading = heading;
		this.textId = textId;
		this.textArguments = textArguments;
	}

	public CharSequence text() {
		return StringResource.format(textId, textArguments);
	}
}
