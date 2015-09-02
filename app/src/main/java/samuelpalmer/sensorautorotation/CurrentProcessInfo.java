package samuelpalmer.sensorautorotation;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import samuelpalmer.sensorautorotation.processes.service.ServiceProcess;

public class CurrentProcessInfo {
	public static String currentProcessName() {
		try {
			return cmdGetProcName();
		}
		catch (Exception ex) {
			log(ex);
			return am();
		}
	}

	private static String cmdGetProcName() {
		BufferedReader cmdlineReader = null;
		try {
			cmdlineReader = new BufferedReader(
				new InputStreamReader(new FileInputStream("/proc/" + getPid() + "/cmdline"), "iso-8859-1")
			);

			StringBuilder processName = new StringBuilder();
			while (true) {
				int c = cmdlineReader.read();
				if (c <= 0)
					break;

				processName.append((char) c);
			}

			String result = processName.toString().trim();
			if (result.isEmpty())
				throw new RuntimeException("Got empty process name.");

			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cmdlineReader != null)
				try {
					cmdlineReader.close();
				} catch (IOException e) {
					log(e);
				}
		}
	}

	private static void log(Exception ex) {
		Log.w(ServiceProcess.class.getSimpleName(), ex);
	}

	private static String am() {
		int pid = getPid();

		for (RunningAppProcessInfo process : SystemServices.activityManager().getRunningAppProcesses())
			if (process.pid == pid) {
				String result = process.processName;
				if (result == null)
					throw new RuntimeException("Current process name was null");

				return result;
			}

		throw new RuntimeException("Couldn't find current process name. Process ID: " + pid);
	}

	private static int getPid() {
		return android.os.Process.myPid();
	}
}
