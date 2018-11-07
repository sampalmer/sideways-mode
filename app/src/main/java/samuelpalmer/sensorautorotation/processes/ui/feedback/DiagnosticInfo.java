package samuelpalmer.sensorautorotation.processes.ui.feedback;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.WindowOrientationListener;

class DiagnosticInfo {

	public static String deviceInfo(Context context) {
		return String.format(Locale.US, "Device: %s %s (%s/%s/%s)\nOS: Android %s %s (%s) (SDK %d)\nLanguage: %s",
			Build.BRAND, Build.MODEL, Build.DEVICE, Build.PRODUCT, Build.HARDWARE,
			VERSION.RELEASE, VERSION.CODENAME, VERSION.INCREMENTAL, VERSION.SDK_INT,
			getPrimaryLanguageName(context) // This is so I can reply to the user in their own language
		);
	}
	
	private static String getPrimaryLanguageName(Context context) {
		Configuration configuration = context.getResources().getConfiguration();
		
		Locale locale;
		if (Build.VERSION.SDK_INT >= 24) {
			LocaleList locales = configuration.getLocales();
			locale = locales.size() <= 0 ? null : locales.get(0);
		}
		else
			//noinspection deprecation
			locale = configuration.locale;
		
		if (locale == null)
			return "Unknown";
		else
			return locale.getDisplayName(Locale.ENGLISH);
	}
	
	/**
	 * Best effort
	 *
	 * @return The logcat output including standard error output
	 */
	public static String logcat() {
		try {
			java.lang.Process process = new ProcessBuilder("logcat", "-d", "-v", "threadtime")
				.redirectErrorStream(true)
				.start();

			//This log message is needed to prevent logcat from hanging if it doesn't have anything to dump, which occurs on Sony Xperia M (Android 4.1.2)
			Log.i(DiagnosticInfo.class.getSimpleName(), "Dumping logcat");

			return dump(process.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String dump(InputStream stream) throws IOException {
		StringBuilder result = new StringBuilder(8192);

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;

				result
					.append(line)
					.append('\n');
			}
		}
		finally {
			try {
				reader.close();
			} catch (IOException ex) {
				// No big deal if we can't close the stream. At least we managed to get the data out of it.
				Log.w(DiagnosticInfo.class.getSimpleName(), ex);
			}
		}

		return result.toString();
	}
	
	public static String sensorDump(Context context) {
		StringBuilder result = new StringBuilder();
		
		SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		
		for (Sensor sensor : sensors) {
			result.append(WindowOrientationListener.stringify(sensor));
			result.append('\n');
		}
		
		return result.toString();
	}
	
}
