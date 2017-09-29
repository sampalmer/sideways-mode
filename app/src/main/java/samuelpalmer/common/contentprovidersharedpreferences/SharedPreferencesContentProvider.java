package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class SharedPreferencesContentProvider extends ContentProvider {
	
	static final String KEY_RESULT = "result";
	static final String KEY_ERROR = "error";
	
	@Override
	public boolean onCreate() {
		// This code is run prior to Application.onCreate, so I'm adding Crashlytics here as well
		Fabric.with(getContext(), new Crashlytics());
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bundle call(@NonNull String method, String sharedPreferencesName, Bundle extras) {
		Bundle resultWrapper = new Bundle();
		
		try {
			Contract contract = ContractMap.lookup(method);
			Object args = contract.deserialiseArgs(extras);
			@SuppressWarnings("ConstantConditions")
			SharedPreferences preferences = getContext().getSharedPreferences(sharedPreferencesName, 0);

			Object result = contract.process(preferences, args);
			Bundle serialisedResult = contract.serialiseResult(result);
			if (serialisedResult == null)
				throw new RuntimeException("Serialised result was null.");

			// Sometimes Android sends back an empty bundle to the client when the content provider code crashes.
			// So we're wrapping the result in a bundle to ensure all valid results look different to an error result.
			resultWrapper.putBundle(KEY_RESULT, serialisedResult);
		}
		catch (Throwable error) {
			// Android appears to swallow errors thrown here, which prevents them from being reported to Google Play.
			// So we'll send them to the client to throw them from there instead.
			resultWrapper.putSerializable(KEY_ERROR, error);
		}
		
		return resultWrapper;
	}

	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		throw notSupported();
	}

	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		throw notSupported();
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw notSupported();
	}

	private RuntimeException notSupported() {
		return new RuntimeException("Not supported");
	}

}
