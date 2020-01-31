package samuelpalmer.sensorautorotation.processes.service.screen;

import java.util.ArrayList;

class ExternalRotationChangeRateLimiter {

    private static final int EXTERNAL_ROTATION_CHANGE_HISTORY_SIZE = 3;

    private final ArrayList<Long> externalRotationChangeTimestamps;

    public ExternalRotationChangeRateLimiter() {
        if (EXTERNAL_ROTATION_CHANGE_HISTORY_SIZE < 2)
            throw new RuntimeException("Need at least two data points to calculate rate of change");

        externalRotationChangeTimestamps = new ArrayList<>(EXTERNAL_ROTATION_CHANGE_HISTORY_SIZE);
    }

    public void registerExternalRotationChange() {
        long nowMillis = System.currentTimeMillis();

        while (externalRotationChangeTimestamps.size() >= EXTERNAL_ROTATION_CHANGE_HISTORY_SIZE) {
            // The history is full, so we'll remove the oldest item
            externalRotationChangeTimestamps.remove(0);
        }

        externalRotationChangeTimestamps.add(nowMillis);
    }

    public Float getExternalRotationChangesPerSecond() {
        if (externalRotationChangeTimestamps.size() < EXTERNAL_ROTATION_CHANGE_HISTORY_SIZE)
            return null; // Can't calculate rate of change without any timestamps

        long oldestTimestamp = externalRotationChangeTimestamps.get(0);
        long newestTimestamp = externalRotationChangeTimestamps.get(externalRotationChangeTimestamps.size() - 1);

        int numberOfChanges = externalRotationChangeTimestamps.size();
        long windowMillis = newestTimestamp - oldestTimestamp;

        if (windowMillis == 0) {
            // We got at least two changes at the same time, so the rate is extremely high
            return Float.POSITIVE_INFINITY;
        }

        return (float)numberOfChanges * 1000f / (float)windowMillis;
    }

}
