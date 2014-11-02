package samuelpalmer.sensorautorotation;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final ServiceController service = new ServiceController(this);
        
        Button start = (Button)findViewById(R.id.start_service);
        start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				service.start();
			}
        	
        });
        
        Button stop = (Button)findViewById(R.id.stop_service);
        stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				service.stop();
			}

        });
    }
}
