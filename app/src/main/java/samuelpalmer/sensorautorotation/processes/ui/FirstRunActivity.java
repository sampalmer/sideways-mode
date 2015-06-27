package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import samuelpalmer.sensorautorotation.R;

public class FirstRunActivity extends ServiceSyncActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_run);

		Button ok = (Button) findViewById(R.id.ok_button);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		setResult(RESULT_OK);
	}

}
