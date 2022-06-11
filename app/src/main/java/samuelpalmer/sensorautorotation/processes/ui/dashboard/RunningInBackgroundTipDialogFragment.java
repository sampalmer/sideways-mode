package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import samuelpalmer.sensorautorotation.R;

/**
 * Dialog that tells the user that the app is running in the background
 */
@SuppressWarnings("WeakerAccess")
public class RunningInBackgroundTipDialogFragment extends AppCompatDialogFragment {

    private OnCompletionListener onCompletionListener;

    public interface OnCompletionListener {
        void onCompleted();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnCompletionListener)
            onCompletionListener = (OnCompletionListener) parentFragment;
        else if (context instanceof OnCompletionListener)
            onCompletionListener = (OnCompletionListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.tip_running_in_background_title, getText(R.string.app_name_full)))
                .setMessage(getString(R.string.tip_running_in_background_description))
                .setPositiveButton(getString(R.string.action_got_it), (dialog, which) -> complete())
                .create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        complete();
    }

    private void complete() {
        if (onCompletionListener != null)
            onCompletionListener.onCompleted();
    }
}
