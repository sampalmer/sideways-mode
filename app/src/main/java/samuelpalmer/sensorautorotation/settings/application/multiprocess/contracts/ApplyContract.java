package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.EditorArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.EditorContract;

public class ApplyContract extends EditorContract<Void> {

	@Override
	public String methodName() {
		return "apply";
	}

	@Override
	public Void process(SharedPreferences sharedPreferences, EditorArgs editorArgs) {
		Editor editor = editorArgs.applyTo(sharedPreferences);
		editor.apply();
		return null;
	}

	@Override
	public Bundle serialiseResult(Void value) {
		return null;
	}

	@Override
	public Void deserialiseResult(Bundle serialised) {
		return null;
	}

}
