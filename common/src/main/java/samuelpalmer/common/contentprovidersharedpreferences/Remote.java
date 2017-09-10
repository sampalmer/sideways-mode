package samuelpalmer.common.contentprovidersharedpreferences;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

class Remote {
	
	private static final int MAX_RETRIES = 3;
	
	private final String preferencesName;
	private final String authority;
	private final ContentResolver contentResolver;

	public Remote(Context context, String authority, String preferencesName) {
		this.preferencesName = preferencesName;
		this.authority = authority;
		contentResolver = context.getContentResolver();
	}

	public <TArgs, TResult> TResult process(Contract<TArgs, TResult> contract, TArgs args) {
		for (int i = 0; i < MAX_RETRIES; ++i) {
			Bundle resultWrapper = contentResolver.call(
				new Uri.Builder()
					.scheme("content")
					.authority(authority)
					.build(),
				contract.methodName(),
				preferencesName,
				contract.serialiseArgs(args)
			);
			
			if (resultWrapper != null) { // I'm pretty sure I've seen this set to null when the content provider crashed.
				Throwable error = (Throwable) resultWrapper.getSerializable(SharedPreferencesContentProvider.KEY_ERROR);
				if (error != null)
					if (error instanceof RuntimeException)
						// Not wrapping the error unnecessarily since Google Play Console only shows the first inner exception;
						throw (RuntimeException) error;
					else
						throw new RuntimeException("Error occurred in content provider", error);
				
				Bundle serialisedResult = resultWrapper.getBundle(SharedPreferencesContentProvider.KEY_RESULT);
				if (serialisedResult != null) // Sometimes when the content provider crashes, it returns an empty bundle.
					return contract.deserialiseResult(serialisedResult);
			}
			
			// This situation happens occasionally in Production, and I don't know exactly why.
			// Retrying multiple times just in case it's an intermittent thing.
			Log.w(this.getClass().getSimpleName(), "Didn't get a result when remotely calling " + contract.methodName() + "(). The content provider probably crashed.");
		}
		
		throw new RuntimeException("Didn't get a result when remotely calling " + contract.methodName() + "() after " + MAX_RETRIES + " retries. Giving up.");
	}

}
