package samuelpalmer.sensorautorotation.instructions;

import android.widget.TextView;

import com.caverock.androidsvg.SVGImageView;

import samuelpalmer.sensorautorotation.StringResource;

public class InstructionsPage {
	private final int textId;
	private final Object[] textArguments;
	private String backgroundSvgFileName;

	public InstructionsPage(String backgroundSvgFileName, int textId, Object... textArguments) {
		this.backgroundSvgFileName = backgroundSvgFileName;
		this.textId = textId;
		this.textArguments = textArguments;
	}

	public void applyText(TextView pageText) {
		pageText.setText(StringResource.format(textId, textArguments));
	}

	public void setBackground(SVGImageView background) {
		background.setImageAsset(backgroundSvgFileName);
	}
}
