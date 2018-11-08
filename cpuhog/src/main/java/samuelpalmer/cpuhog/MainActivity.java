package samuelpalmer.cpuhog;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private CpuHog cpuHog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cpuHog = new CpuHog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cpuHog.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cpuHog.stop();
    }

}
