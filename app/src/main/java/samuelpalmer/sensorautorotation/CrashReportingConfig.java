package samuelpalmer.sensorautorotation;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public abstract class CrashReportingConfig {
	
	public static void init(Context context) {
		// Set up Crashlytics, disabled for debug builds
		Crashlytics crashlyticsKit = new Crashlytics.Builder()
			.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
			.build();
		
		// Initialize Fabric with the debug-disabled crashlytics.
		Fabric.with(context, crashlyticsKit);
	}
	
}
