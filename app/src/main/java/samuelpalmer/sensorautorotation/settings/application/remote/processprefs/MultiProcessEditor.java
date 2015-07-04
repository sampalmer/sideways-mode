package samuelpalmer.sensorautorotation.settings.application.remote.processprefs;

import android.content.SharedPreferences.Editor;

import java.util.Set;

import samuelpalmer.sensorautorotation.settings.application.remote.processprefs.SemaphoreLocker.Operation;

class MultiProcessEditor implements Editor {

	private final Editor editor;
	private final SemaphoreLocker locker;

	public MultiProcessEditor(Editor editor, SemaphoreLocker locker) {
		this.editor = editor;
		this.locker = locker;
	}

	public Editor putString(String key, String value) {
		return editor.putString(key, value);
	}

	public Editor putStringSet(String key, Set<String> values) {
		return editor.putStringSet(key, values);
	}

	public Editor putInt(String key, int value) {
		return editor.putInt(key, value);
	}

	public Editor putLong(String key, long value) {
		return editor.putLong(key, value);
	}

	public Editor putFloat(String key, float value) {
		return editor.putFloat(key, value);
	}

	public Editor putBoolean(String key, boolean value) {
		return editor.putBoolean(key, value);
	}

	public Editor remove(String key) {
		return editor.remove(key);
	}

	public Editor clear() {
		return editor.clear();
	}

	public boolean commit() {
		return locker.lock(new Operation<Boolean>() {

			@Override
			public Boolean run() {
				return editor.commit();
			}

		});
	}

	public void apply() {
		commit();
	}

}
