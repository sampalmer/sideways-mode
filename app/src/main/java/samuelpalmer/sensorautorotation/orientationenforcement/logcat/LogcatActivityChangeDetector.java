package samuelpalmer.sensorautorotation.orientationenforcement.logcat;

import android.content.ComponentName;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;

/**
 * Still not quite quick enough for seamless app switches with enforcement changes, but getting there
 */
public class LogcatActivityChangeDetector extends State {

	// Events that have both a task id and component name, taken from the following URL
	// https://android.googlesource.com/platform/frameworks/base/+/android-4.4_r1/services/java/com/android/server/am/EventLogTags.logtags
	private static final List<String> taskToPackageNameEvents = Arrays.asList(
			"am_new_intent",
			"am_create_activity",
			"am_restart_activity",
			"am_resume_activity",
			"am_relaunch_resume_activity",
			"am_relaunch_activity"
	);

	private final ActivityMonitor activityMonitor;

	public LogcatActivityChangeDetector(ActivityMonitor activityMonitor) {
		this.activityMonitor = activityMonitor;
	}

	@Override
	protected void start() {
		new AsyncTask<Void, String, Void>() {

			@Override
			protected void onProgressUpdate(String... values) {
				activityMonitor.setCurrentApp(values[0]);
			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Process process = Runtime.getRuntime().exec("su");

					PrintStream outputStream = null;
					try {
						outputStream = new PrintStream(new BufferedOutputStream(process.getOutputStream(), 8192));
						outputStream.println(String.format("logcat -b events -b main %s wm_task_moved *:S", TextUtils.join(" ", taskToPackageNameEvents)));
						outputStream.flush();
					} finally {
						if (outputStream != null)
							outputStream.close();
					}

					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

					HashMap<String, String> taskIdsToPackageNames = new HashMap<String, String>();

					do {
						String line = bufferedReader.readLine();
						if (line == null)
							throw new RuntimeException("Logcat finished:\n" + readError(process));

						// Skip lines that look like the following.
						// These showed up when multiple buffers were listened to on Cyanogenmod 12.
						// --------- beginning of events
						if (line.charAt(0) == '-')
							continue;

						LogMessage log = new LogMessage(line);


						//Mapping
						if (taskToPackageNameEvents.contains(log.tag())) {
							String message = log.message();
							String commaDelimitedValues = message.substring(1, message.length() - 1);
							String[] values = commaDelimitedValues.split(",");
							String taskId = values[2];
							ComponentName componentName = ComponentName.unflattenFromString(values[3]);
							taskIdsToPackageNames.put(taskId, componentName.getPackageName());
						}

						switch (log.tag()) {
							case "wm_task_moved":
								int startIndex = line.indexOf('[') + 1;
								int endIndex = line.indexOf(',', startIndex + 1);
								String taskId = line.substring(startIndex, endIndex);
								String packageName = taskIdsToPackageNames.get(taskId);
								if (packageName != null)
									publishProgress(packageName);
								break;
						}

					} while (true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}.execute();
	}

	@Override
	protected void stop() {

	}

	private static String readError(Process process) throws IOException {
		StringBuilder error = new StringBuilder();
		BufferedReader e = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		do {
			String errorLine = e.readLine();
			if (errorLine == null)
				break;

			error.append(errorLine);
		} while (true);

		return error.toString();
	}

}
