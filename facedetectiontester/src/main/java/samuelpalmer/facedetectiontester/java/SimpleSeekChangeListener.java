package samuelpalmer.facedetectiontester.java;

import android.widget.SeekBar;

class SimpleSeekChangeListener implements SeekBar.OnSeekBarChangeListener {

    private final Listener listener;

    public SimpleSeekChangeListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            listener.onProgressChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    public interface Listener {
        void onProgressChanged(int progress);
    }

}
