package samuelpalmer.sensorautorotation;

import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class DiagnosticInfo {

	public static String deviceInfo() {
		return String.format(Locale.US, "%s %s (%s/%s/%s)\nAndroid %s %s (%s) (SDK %d)",
			Build.BRAND, Build.MODEL, Build.DEVICE, Build.PRODUCT, Build.HARDWARE,
			VERSION.RELEASE, VERSION.CODENAME, VERSION.INCREMENTAL, VERSION.SDK_INT
		);
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
			} catch (IOException ignored) {
				Log.w(DiagnosticInfo.class.getSimpleName(), ignored);
			}
		}

		return result.toString();
	}
}
