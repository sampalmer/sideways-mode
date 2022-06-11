package samuelpalmer.sensorautorotation.utilities;

import android.content.Intent;

public class BroadcastKillFix {

    private BroadcastKillFix() {}

    /**
     * Prevents the receiving process from being killed by the OS when it receives the broadcast
     * after task removal in Android 4.1 and above.
     */
    public static void apply(Intent intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
    }

}
