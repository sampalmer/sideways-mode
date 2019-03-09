package android.view;

import android.annotation.SuppressLint;
import samuelpalmer.sensorautorotation.processes.service.screen.RotationWatcherRegistrar.RotationWatcherNotSupportedException;

/**
 * Copied from Android source code. Actual implementation shouldn't be used; this is present to
 * trick our code into running the hidden Android implementation.
 */
@SuppressWarnings("ALL") // Suppressing everything since this is AOSP code and I want it to stay the same as the original.
@SuppressLint("all")
public interface IRotationWatcher extends android.os.IInterface
{
    public static abstract class Stub extends android.os.Binder implements IRotationWatcher
    {
        public Stub()
        {
            throw new RotationWatcherNotSupportedException("Didn't load the system implementation");
        }

        public static IRotationWatcher asInterface(android.os.IBinder obj)
        {
            return null;
        }

        @Override public android.os.IBinder asBinder()
        {
            return this;
        }

        @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
        {
            return false;
        }
    }

    public void onRotationChanged(int rotation) throws android.os.RemoteException;
}
