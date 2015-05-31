package samuelpalmer.sensorautorotation.instructions;

import android.widget.TextView;

import samuelpalmer.sensorautorotation.StringResource;

public class InstructionsPage {
	private final int textId;
	private final Object[] textArguments;

	public InstructionsPage(int textId, Object... textArguments) {
		this.textId = textId;
		this.textArguments = textArguments;
	}

	public void applyText(TextView pageText) {
		pageText.setText(StringResource.format(textId, textArguments));
	}
}
