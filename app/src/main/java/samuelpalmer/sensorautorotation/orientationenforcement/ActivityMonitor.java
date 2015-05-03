package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.orientationenforcement.logcat.LogMessage;

public class ActivityMonitor extends State {

	// Events that have both a task id and component name, taken from the following URL
	// https://android.googlesource.com/platform/frameworks/base/+/android-4.4_r1/services/java/com/android/server/am/EventLogTags.logtags
	private static final List<String> taskToPackageNameEvents = Arrays.asList(
		"am_finish_activity",
		"am_new_intent",
		"am_create_activity",
		"am_restart_activity",
		"am_resume_activity",
		"am_destroy_activity",
		"am_relaunch_resume_activity",
		"am_relaunch_activity"
	);

	private final HashMap<String, String> taskIdsToPackageNames;
	private final Handler handler;
	private String lastPackage;

	public ActivityMonitor(Context context) {
		this.taskIdsToPackageNames = new HashMap<>();
		handler = new Handler(context.getMainLooper());
	}

	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
								if (packageName != null && !packageName.equals(lastPackage)) {
									lastPackage = packageName;
									handler.post(reportChange);
								}
								break;
						}
					} while (true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
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

	public void stop() {

	}

	public Collection<String> current() {
		return Collections.singletonList(lastPackage);
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();

	private final Runnable reportChange = new Runnable() {
		@Override
		public void run() {
			onChange.report(null);
		}
	};

}
