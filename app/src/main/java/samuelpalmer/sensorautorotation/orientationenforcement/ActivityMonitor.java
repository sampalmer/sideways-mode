package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.os.Handler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {

	private final Context context;
	private final ConcurrentHashMap<Integer, String> taskIdsToPackageNames;
	private String lastPackage;

	public ActivityMonitor(Context context) {
		this.context = context;
		this.taskIdsToPackageNames = new ConcurrentHashMap<>();
	}

	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Process process = new ProcessBuilder("su")
							.redirectErrorStream(true)
							.start();

					PrintStream outputStream = null;
					try {
						outputStream = new PrintStream(new BufferedOutputStream(process.getOutputStream(), 8192));

						String now = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
						outputStream.println(String.format("logcat -b events -T '%s' am_new_intent am_resume_activity am_relaunch_resume_activity am_restart_activity *:S", now));
						outputStream.flush();
					} finally {
						if (outputStream != null) {
							outputStream.close();
						}
					}

					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

					do {
						String line = bufferedReader.readLine();
						if (line == null) {
							StringBuilder r = new StringBuilder();
							BufferedReader e = new BufferedReader(new InputStreamReader(process.getInputStream()));
							do {
								String eL = e.readLine();
								if (eL == null)
									break;

								r.append(eL);
							} while (true);

							throw new RuntimeException("Logcat finished: " + r.toString());
						}

						String[] components = line.split(",");
						String componentName = components[3];
						final String packageName = componentName.split("/")[0];
						new Handler(context.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								if (!packageName.equals(lastPackage)) {
									lastPackage = packageName;
									onChange.report(null);
								}
							}
						});
					} while (true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
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
}
