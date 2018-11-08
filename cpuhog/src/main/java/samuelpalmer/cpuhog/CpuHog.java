package samuelpalmer.cpuhog;

import android.os.Process;
import android.util.Log;

public class CpuHog {

    private static final String TAG = CpuHog.class.getSimpleName();

    private boolean isStarted;

    public void start() {
        if (isStarted)
            throw new IllegalStateException("Already started");

        int numberOfProcessors = Runtime.getRuntime().availableProcessors();
        Log.i(TAG, String.format("Found %d processors", numberOfProcessors));

        isStarted = true;

        int threadCount = numberOfProcessors * 2;
        Log.i(TAG, String.format("Starting %d threads", threadCount));

        for (int i = 0; i < threadCount; ++i) {
            SpinnerThread thread = new SpinnerThread(i);
            thread.start();
            Log.i(TAG, String.format("Thread #%d (%d) started", thread.threadNumber, thread.getId()));
        }
    }

    public void stop() {
        if (!isStarted)
            throw new RuntimeException("Not started");

        Log.i(TAG, "Stopping all threads");

        // Android doesn't let you kill threads.
        // Another option is having the threads check whether they're meant to keep running, but this prevents them from maxing out the CPU.
        // So killing the process seems best for this situation.
        Process.killProcess(Process.myPid());
    }

    private class SpinnerThread extends Thread  {

        private final int threadNumber;

        public SpinnerThread(int threadNumber) {
            this.threadNumber = threadNumber;
        }

        @Override
        public void run() {
            int reps = 2000 + threadNumber;
            int ret = 0;

            //noinspection InfiniteLoopStatement
            while (true)
                for (double i = 0; i < reps; i += 0.67)
                    for (double j = 0; j < 100000; j += 1.57)
                        ret += i * Math.sin(j);
        }

    }

}
