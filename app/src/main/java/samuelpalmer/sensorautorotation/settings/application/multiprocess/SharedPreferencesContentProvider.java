package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.Process;

public class SharedPreferencesContentProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Contract contract = ContractMap.lookup(method);
		Object args = contract.deserialiseArgs(extras);
		Object result = contract.process(Process.context().getSharedPreferences(arg, 0), args);
		return contract.serialiseResult(result);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		throw notSupported();
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw notSupported();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	private RuntimeException notSupported() {
		return new RuntimeException("Not supported");
	}

}
