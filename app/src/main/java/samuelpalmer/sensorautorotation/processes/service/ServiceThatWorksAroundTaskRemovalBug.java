package samuelpalmer.sensorautorotation.processes.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.utilities.BroadcastKillFix;

abstract class ServiceThatWorksAroundTaskRemovalBug extends Service {

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        clearImmediateDeath();
        clearPendingDeath();
    }

    /**
     * Works around a bug in which swiping away the UI makes Android kill this process immediately.
     *
     * https://code.google.com/p/android/issues/detail?id=178057
     */
    private void clearImmediateDeath() {
        // Some users got ANRs when the broadcast receiver actually did work,
        // so now we just use a receiver that doesn't do anything.
        Intent intent = new Intent(this, DummyReceiver.class);
        BroadcastKillFix.apply(intent);
        
        //This seems to be timing-related; the more times we do this, the less likely the process gets killed
        for (int i = 0; i < 50; ++i)
            sendBroadcast(intent);
    }

    /**
     * Resets the process's "waitingToKill" flag so it won't be killed on the next received broadcast.
     * This has a delayed effect since it only seems to apply once the activity has actually started.
     *
     * https://code.google.com/p/android/issues/detail?id=104308
     */
    private void clearPendingDeath() {
        Intent intent = new Intent(this, DummyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Using a PendingIntent works around the issue where pressing the home button causes a delay in activity starts (https://issuetracker.google.com/issues/36910222)
        // But still keeping the 'startActivity' call (above) since it probably has less latency.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PendingIntentIds.TASK_REMOVAL_FIX, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            throw new RuntimeException(e);
        }
    }

}
